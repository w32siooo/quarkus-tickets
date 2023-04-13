package cygni.es.mappers;

import cygni.es.AggregateRoot;
import cygni.es.Event;
import cygni.es.SerializerUtils;
import cygni.es.Snapshot;
import cygni.es.orm.EventEntity;
import cygni.es.orm.SnapshotEntity;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import org.slf4j.Logger;

import javax.ws.rs.core.SecurityContext;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EventSourcingMappers {
  private static final Logger log = org.slf4j.LoggerFactory.getLogger(EventSourcingMappers.class);
  private EventSourcingMappers() {}

  public static UUID uuidFromSecurityContext(SecurityContext securityContext) {
    var ctx = securityContext.getUserPrincipal().toString();
    Pattern pattern = Pattern.compile("(?<=subject=').{36}");
    Matcher matcher = pattern.matcher(ctx);
    if (matcher.find()) {
      return UUID.fromString(matcher.group());
    }
    else {
      throw new RuntimeException("Could not find UUID in SecurityContext");
    }
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
