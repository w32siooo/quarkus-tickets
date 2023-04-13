package cygni.es.mappers;

import cygni.es.AggregateRoot;
import cygni.es.Event;
import cygni.es.SerializerUtils;
import cygni.es.Snapshot;
import cygni.es.orm.EventEntity;
import cygni.es.orm.SnapshotEntity;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;

import javax.ws.rs.core.SecurityContext;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public class EventSourcingMappers {
  private EventSourcingMappers() {}

  public static UUID uuidFromSecurityContext(SecurityContext securityContext) {
    var ctx = securityContext.getUserPrincipal().toString();
    var id = ctx.substring(ctx.indexOf("id='") + 4, ctx.indexOf("',"));
    return UUID.fromString(id);
  }

  public static <T extends AggregateRoot> Snapshot snapshotFromAggregate(final T aggregate) {
    byte[] bytes = SerializerUtils.serializeToJsonBytes(aggregate);
    return new Snapshot(
        UUID.randomUUID(),
        aggregate.getId(),
        aggregate.getType(),
        bytes,
        null,
        aggregate.getVersion(),
        OffsetDateTime.now());
  }

  public static Snapshot snapshotFromEntity(final SnapshotEntity entity) {
    return new Snapshot(
        entity.getId(),
        entity.getAggregateId(),
        entity.getAggregateType(),
        entity.getData(),
        entity.getMetadata(),
        entity.getVersion(),
        entity.getTimestamp());
  }

  public static Uni<List<Event>> eventsFromEntities(final List<EventEntity> entities) {
    return Multi.createFrom()
        .iterable(entities)
        .map(EventSourcingMappers::eventFromEntity)
        .collect()
        .asList();
  }

  public static Event eventFromEntity(final EventEntity entity) {
    return new Event(
        entity.getId(),
        entity.getAggregateId(),
        entity.getAggregateType(),
        entity.getEventType(),
        entity.getData(),
        entity.getMetadata(),
        entity.getVersion(),
        entity.getTimestamp());
  }

  public static <T extends AggregateRoot> T aggregateFromSnapshot(
      final Snapshot snapshot, final Class<T> valueType) {
    return SerializerUtils.deserializeFromJsonBytes(snapshot.getData(), valueType);
  }
}
