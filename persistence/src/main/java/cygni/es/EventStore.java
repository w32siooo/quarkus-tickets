package cygni.es;

import cygni.es.mappers.EventSourcingMappers;
import cygni.es.orm.EventEntity;
import cygni.es.orm.SnapshotEntity;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.unchecked.Unchecked;
import java.lang.reflect.InvocationTargetException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import org.hibernate.reactive.mutiny.Mutiny;
import org.slf4j.Logger;

@ApplicationScoped
public class EventStore implements EventStoreDB {

  private final Logger log = org.slf4j.LoggerFactory.getLogger(EventStore.class);

  private static final String LOAD_EVENTS_QUERY =
      "select * from events e where e.aggregate_id = ?1 and e.version > ?2 ORDER BY e.version ASC";
  private static final String HANDLE_CONCURRENCY_QUERY =
      "SELECT CAST(aggregate_id as varchar) FROM events e WHERE e.aggregate_id = ?1 LIMIT 1 FOR UPDATE";

  private static final String LOAD_DISTINCT_AGG_IDS_QUERY =
      "select distinct CAST(aggregate_id as varchar) from events where aggregate_type = ?1";
  private final int SNAPSHOT_FREQUENCY = 3;

  private static final String SAVE_SNAPSHOT_QUERY =
      "INSERT INTO snapshots (aggregate_id, aggregate_type, data, metadata, version, timestamp,id) "
          + "VALUES (?1, ?2, ?3, ?4, ?5, now(), ?6) "
          + "ON CONFLICT (aggregate_id) "
          + "DO UPDATE SET data = $3, version = $5, timestamp = now()";
  @Inject EventBus eventBus;

  @Inject Mutiny.SessionFactory sf;

  @Override
  public Uni<List<Event>> loadEvents(UUID aggregateId, long version) {
    return sf.withSession(
        session ->
            session
                .createNativeQuery(LOAD_EVENTS_QUERY, EventEntity.class)
                .setParameter(1, aggregateId)
                .setParameter(2, version)
                .getResultList()
                .flatMap(EventSourcingMappers::eventsFromEntities));
  }

  private <T extends AggregateRoot> T getAggregate(
      final UUID aggregateId, final Class<T> aggregateType) {
    try {
      return aggregateType.getConstructor(UUID.class).newInstance(aggregateId);
    } catch (InstantiationException
        | IllegalAccessException
        | InvocationTargetException
        | NoSuchMethodException e) {
      throw new RuntimeException(e);
    }
  }

  private <T extends AggregateRoot> Uni<T> raiseAggregateEvents(T aggregate, List<Event> events) {
    Uni<T> fallBackUni =
        (aggregate.getVersion() == 0)
            ? Uni.createFrom().failure(new IllegalAccessError("agg not found"))
            : Uni.createFrom().item(aggregate);
    return Multi.createFrom()
        .iterable(events)
        .onItem()
        .invoke(aggregate::raiseEvent)
        .collect()
        .last()
        .onItem()
        .ifNotNull()
        .transform(s -> aggregate)
        .onItem()
        .ifNull()
        .switchTo(fallBackUni);
  }

  @Override
  public <T extends AggregateRoot> Uni<T> load(UUID aggregateId, Class<T> aggregateType) {
    return getSnapshot(aggregateId)
        .map(snapshot -> getSnapshotFromClass(snapshot, aggregateId, aggregateType))
        .onItem()
        .ifNotNull()
        .transformToUni(
            s ->
                loadEvents(aggregateId, s.getVersion())
                    .chain(events -> raiseAggregateEvents(s, events)))
        .onItem()
        .ifNull()
        .failWith(
            Unchecked.supplier(
                () -> {
                  log.error(
                      "Aggregate not found: "
                          + aggregateId
                          + " of type: "
                          + aggregateType.getName());
                  throw new IllegalAccessError(aggregateId.toString());
                }));
  }

  public <T extends AggregateRoot> Uni<List<T>> loadAll(
      Class<T> aggregateClass, String aggregateName) {
    return sf.withSession(
        session ->
            session
                .createNativeQuery(LOAD_DISTINCT_AGG_IDS_QUERY, String.class)
                .setParameter(1, aggregateName)
                .getResultList()
                .flatMap(
                    s ->
                        Multi.createFrom()
                            .iterable(s)
                            .onItem()
                            .transformToUni(
                                aggregateId -> load((UUID.fromString(aggregateId)), aggregateClass))
                            .concatenate()
                            .collect()
                            .asList()));
  }

  private <T extends AggregateRoot> T getSnapshotFromClass(
      Snapshot snapshot, UUID aggregateId, Class<T> aggregateType) {
    if (snapshot == null) {
      final var defaultSnapshot =
          EventSourcingMappers.snapshotFromAggregate(getAggregate(aggregateId, aggregateType));
      return EventSourcingMappers.aggregateFromSnapshot(defaultSnapshot, aggregateType);
    }
    return EventSourcingMappers.aggregateFromSnapshot(snapshot, aggregateType);
  }

  public Uni<Snapshot> getSnapshot(UUID aggregateId) {
    return sf.withSession(
        session ->
            session
                .find(SnapshotEntity.class, aggregateId)
                .onItem()
                .ifNotNull()
                .transform(EventSourcingMappers::snapshotFromEntity));
  }

  @Override
  public <T extends AggregateRoot> Uni<Void> persistAndPublish(T aggregate) {
    final List<Event> changes = new ArrayList<>(aggregate.getChanges());
    return sf.withTransaction(
            session ->
                session
                    .createNativeQuery(HANDLE_CONCURRENCY_QUERY, UUID.class)
                    .setParameter(1, aggregate.getId())
                    .getResultList()
                    .chain(s -> persistEvents(session, changes))
                    .onItem()
                    .invoke(res -> log.info("Events persisted"))
                    .chain(
                        s ->
                            aggregate.getVersion() % SNAPSHOT_FREQUENCY == 0
                                ? persistSnapshot(session, aggregate)
                                : Uni.createFrom().voidItem()))
        .chain(s -> eventBus.publish(changes))
        .onFailure()
        .invoke(ex -> log.error("publishing exception", ex))
        .onItem()
        .invoke(success -> log.info("events saved successfully: {}", success))
        .replaceWithVoid();
  }

  @Override
  public Uni<Boolean> exists(UUID aggregateId) {
    return sf.withSession(
        session ->
            session.find(EventEntity.class, aggregateId).onItem().transform(Objects::nonNull));
  }

  private <T extends AggregateRoot> Uni<Void> persistSnapshot(Mutiny.Session session, T aggregate) {
    aggregate.toSnapshot();
    final var snapshot = EventSourcingMappers.snapshotFromAggregate(aggregate);


    SnapshotEntity snapshotEntity = new SnapshotEntity(
            UUID.randomUUID(),
            snapshot.getAggregateId(),
        snapshot.getAggregateType(),
        snapshot.getData(),
        snapshot.getMetaData(),
        snapshot.getVersion(),
        OffsetDateTime.now());
    return session
        .createNativeQuery(SAVE_SNAPSHOT_QUERY)
        .setParameter(1, snapshotEntity.getAggregateId())
        .setParameter(2, snapshotEntity.getAggregateType())
        .setParameter(3, snapshotEntity.getData())
        .setParameter(4, snapshotEntity.getMetadata())
        .setParameter(5, snapshotEntity.getVersion())
        .setParameter(6, snapshotEntity.getId())
        .executeUpdate()
        .replaceWithVoid()
        .onItem()
        .invoke(res -> log.info("Snapshot persisted"));
  }

  public Uni<Void> persistEvents(Mutiny.Session session, List<Event> events) {
    return Multi.createFrom()
        .iterable(events)
        .map(
            event ->
                    new EventEntity(
                        event.getAggregateId(),
                        event.getAggregateType(),
                        event.getType(),
                        event.getData(),
                        event.getMetadata(),
                        event.getVersion(),
                        OffsetDateTime.now()))
        .collect()
        .asList()
        .chain(e -> session.persistAll(e.toArray()))
        .replaceWithVoid();
  }
}
