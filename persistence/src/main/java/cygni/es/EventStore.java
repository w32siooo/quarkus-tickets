package cygni.es;


import cygni.es.orm.EventEntity;
import cygni.es.orm.SnapshotEntity;
import cygni.es.mappers.EventSourcingMappers;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.unchecked.Unchecked;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.reactive.mutiny.Mutiny;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.lang.reflect.InvocationTargetException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;


@ApplicationScoped
@Slf4j
public class EventStore implements EventStoreDB {

    private final static String LOAD_EVENTS_QUERY = "select * from events e where e.aggregate_id = ?1 and e.version > ?2 ORDER BY e.version ASC";
    private final static String HANDLE_CONCURRENCY_QUERY = "SELECT aggregate_id FROM events e WHERE e.aggregate_id = ?1 LIMIT 1 FOR UPDATE";
    private final int SNAPSHOT_FREQUENCY = 3;

    private final static String SAVE_SNAPSHOT_QUERY = "INSERT INTO snapshots (aggregate_id, aggregate_type, data, metadata, version, timestamp,id) " +
            "VALUES (?1, ?2, ?3, ?4, ?5, now(), ?6) " +
            "ON CONFLICT (aggregate_id) " +
            "DO UPDATE SET data = $3, version = $5, timestamp = now()";
    @Inject
    EventBus eventBus;


    @Inject
    Mutiny.SessionFactory sf;

    @Override
    public Uni<List<Event>> loadEvents(String aggregateId, long version) {
        return sf.withSession(session -> session.createNativeQuery(LOAD_EVENTS_QUERY, EventEntity.class)
                .setParameter(1, aggregateId)
                .setParameter(2, version)
                .getResultList()
                .flatMap(EventSourcingMappers::eventsFromEntities));
    }

    private <T extends AggregateRoot> T getAggregate(final String aggregateId, final Class<T> aggregateType) {
        try {
            return aggregateType.getConstructor(String.class).newInstance(aggregateId);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                 NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    private <T extends AggregateRoot> Uni<T> raiseAggregateEvents(T aggregate, List<Event> events) {
        Uni<T> fallBackUni = (aggregate.getVersion() == 0) ? Uni.createFrom().failure(new IllegalAccessError("agg not found")) : Uni.createFrom().item(aggregate);
        return Multi.createFrom()
                .iterable(events).onItem().invoke(aggregate::raiseEvent)
                .collect().last()
                .onItem().ifNotNull().transform(s -> aggregate)
                .onItem().ifNull().switchTo(fallBackUni);
    }

    @Override
    public <T extends AggregateRoot> Uni<T> load(String aggregateId, Class<T> aggregateType) {
        return getSnapshot(aggregateId).map(snapshot -> getSnapshotFromClass(snapshot, aggregateId, aggregateType))
                .onItem().ifNotNull().transformToUni(s -> loadEvents(aggregateId, s.getVersion())
                        .chain(events -> raiseAggregateEvents(s, events)))
                .onItem().ifNull().failWith(Unchecked.supplier(() -> {
                    log.error("Aggregate not found: " + aggregateId + " of type: " + aggregateType.getName());
                    throw new IllegalAccessError(aggregateId);
                }));
    }

    public <T extends AggregateRoot> Uni<List<T>> loadAll(Class<T> aggregateClass, String aggregateName)  {
        return sf.withSession(session -> session.createNativeQuery("select * from snapshots where aggregate_type = ?1", SnapshotEntity.class)
                .setParameter(1, aggregateName)
                .getResultList()
                .flatMap(s-> Multi.createFrom().iterable(s)
                                .onItem().transform(EventSourcingMappers::snapshotFromEntity)
                                .onItem().transformToUniAndConcatenate(snap -> loadEvents(snap.getAggregateId(), snap.getVersion())
                                        .chain(events -> raiseAggregateEvents(getSnapshotFromClass(snap, snap.getAggregateId(), aggregateClass), events)))
                                .collect().asList()
                ));
    }

    private <T extends AggregateRoot> T getSnapshotFromClass(Snapshot snapshot, String aggregateId, Class<T> aggregateType) {
        if (snapshot == null) {
            final var defaultSnapshot = EventSourcingMappers.snapshotFromAggregate(getAggregate(aggregateId, aggregateType));
            return EventSourcingMappers.aggregateFromSnapshot(defaultSnapshot, aggregateType);
        }
        return EventSourcingMappers.aggregateFromSnapshot(snapshot, aggregateType);
    }

    public Uni<Snapshot> getSnapshot(String aggregateId) {
        return sf.withSession(session -> session.find(SnapshotEntity.class, UUID.fromString(aggregateId))
                .onItem().ifNotNull().transform(EventSourcingMappers::snapshotFromEntity));
    }

    @Override
    public <T extends AggregateRoot> Uni<Void> persistAndPublish(T aggregate) {
        final List<Event> changes = new ArrayList<>(aggregate.getChanges());
        return sf.withTransaction(session -> session.createNativeQuery(HANDLE_CONCURRENCY_QUERY, String.class)
                        .setParameter(1, aggregate.getId())
                        .getResultList()
                        .chain(s -> persistEvents(session, changes)).onItem().invoke(res -> log.info("Events persisted"))
                        .chain(s -> aggregate.getVersion() % SNAPSHOT_FREQUENCY == 0 ? persistSnapshot(session, aggregate) : Uni.createFrom().voidItem())
                )
                .chain(s -> eventBus.publish(changes))
                .onFailure().invoke(ex -> log.error("publishing exception", ex))
                .onItem().invoke(success -> log.info("events saved successfully: {}", success))
                .replaceWithVoid();
    }


    @Override
    public Uni<Boolean> exists(String aggregateId) {
        return sf.withSession(session ->
                session.find(EventEntity.class, aggregateId).onItem()
                        .transform(Objects::nonNull));
    }

    private <T extends AggregateRoot> Uni<Void> persistSnapshot(Mutiny.Session session, T aggregate) {
        aggregate.toSnapshot();
        final var snapshot = EventSourcingMappers.snapshotFromAggregate(aggregate);
        SnapshotEntity snapshotEntity = SnapshotEntity.builder()
                .aggregateId(snapshot.getAggregateId())
                .aggregateType(snapshot.getAggregateType())
                .data(snapshot.getData())
                .metadata(snapshot.getMetaData())
                .version(snapshot.getVersion())
                .timestamp(OffsetDateTime.now())
                .id(UUID.randomUUID())
                .build();
        return session.createNativeQuery(SAVE_SNAPSHOT_QUERY)
                .setParameter(1, snapshotEntity.getAggregateId())
                .setParameter(2, snapshotEntity.getAggregateType())
                .setParameter(3, snapshotEntity.getData())
                .setParameter(4, snapshotEntity.getMetadata())
                .setParameter(5, snapshotEntity.getVersion())
                .setParameter(6, snapshotEntity.getId())
                .executeUpdate().replaceWithVoid()
                .onItem().invoke(res -> log.info("Snapshot persisted"));
    }

    public Uni<Void> persistEvents(Mutiny.Session session, List<Event> events) {
        return Multi.createFrom().iterable(events).map(event ->
                EventEntity.builder()
                        .aggregateId(event.getAggregateId())
                        .aggregateType(event.getAggregateType())
                        .eventType(event.getEventType())
                        .data(event.getData())
                        .metadata(event.getMetadata())
                        .version(event.getVersion())
                        .timestamp(OffsetDateTime.now())
                        .build()
        ).collect().asList().chain(e -> session.persistAll(e.toArray())).replaceWithVoid();
    }


}