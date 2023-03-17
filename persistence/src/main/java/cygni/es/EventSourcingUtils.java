package cygni.es;

import cygni.orm.EventEntity;
import cygni.orm.SnapshotEntity;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;


public class EventSourcingUtils {
    private EventSourcingUtils() {
    }

    public static <T extends AggregateRoot> Snapshot snapshotFromAggregate(final T aggregate) {
        byte[] bytes = SerializerUtils.serializeToJsonBytes(aggregate);
        return Snapshot.builder()
                .id(UUID.randomUUID())
                .aggregateId(aggregate.getId())
                .aggregateType(aggregate.getType())
                .version(aggregate.getVersion())
                .data(bytes)
                .timestamp(OffsetDateTime.now())
                .build();
    }

    public static Snapshot snapshotFromEntity(final SnapshotEntity entity) {
        return Snapshot.builder()
                .id(entity.getId())
                .aggregateId(entity.getAggregateId())
                .aggregateType(entity.getAggregateType())
                .version(entity.getVersion())
                .data(entity.getData())
                .timestamp(entity.getTimestamp())
                .build();
    }

    public static Uni<List<Event>> eventsFromEntities(final List<EventEntity> entities){
        return Multi.createFrom().iterable(entities)
                .map(EventSourcingUtils::eventFromEntity)
                .collect().asList();

    }

    public static Event eventFromEntity(final EventEntity entity){
        return Event.builder()
                .id(entity.getId())
                .aggregateId(entity.getAggregateId())
                .aggregateType(entity.getAggregateType())
                .eventType(entity.getEventType())
                .data(entity.getData())
                .metadata(entity.getMetadata())
                .version(entity.getVersion())
                .timestamp(entity.getTimestamp())
                .build();
    }

    public static <T extends AggregateRoot> T aggregateFromSnapshot(final Snapshot snapshot, final Class<T> valueType) {
        return SerializerUtils.deserializeFromJsonBytes(snapshot.getData(), valueType);
    }

}
