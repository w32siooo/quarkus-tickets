package cygni.core.eventsourcing;

import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Event {

  public Event(String type, String aggregateType) {
    this.id = UUID.randomUUID();
    this.type = type;
    this.aggregateType = aggregateType;
    this.timestamp = OffsetDateTime.now();
  }

  private UUID id;

  @Type(type = "org.hibernate.type.PostgresUUIDType")
  private UUID aggregateId;

  private String type;

  private String aggregateType;

  private long version;

  private byte[] data;

  private byte[] metadata;

  private OffsetDateTime timestamp;
}
