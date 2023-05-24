package cygni.es.orm;

import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.util.UUID;
import jakarta.persistence.*;
import jakarta.persistence.Entity;
import org.hibernate.annotations.TimeZoneStorage;

@Entity
@Table(
    name = "events",
    uniqueConstraints = @UniqueConstraint(columnNames = {"aggregate_id", "version"}))
public class EventEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private UUID id;

  @Column(name = "aggregate_id", nullable = false)
  private UUID aggregateId;

  @Column(name = "aggregate_type", nullable = false)
  private String aggregateType;

  @Column(name = "event_type", nullable = false)
  private String eventType;

  @Column(name = "data")
  private byte[] data;

  @Column(name = "metadata")
  private byte[] metadata;

  @Version
  @Column(name = "version", nullable = false)
  private Long version;

  @Column(name = "timestamp", nullable = false)
  @TimeZoneStorage
  private ZonedDateTime timestamp;

  public EventEntity() {}
public EventEntity(UUID aggregateId, String aggregateType, String type, byte[] data, byte[] metadata, long version, ZonedDateTime now) {
    this.aggregateId = aggregateId;
    this.aggregateType = aggregateType;
    this.eventType = type;
    this.data = data;
    this.metadata = metadata;
    this.version = version;
    this.timestamp = now;
    }

    public UUID getId() {
        return id;
    }

    public UUID getAggregateId() {
        return aggregateId;
    }

    public String getAggregateType() {
        return aggregateType;
    }

    public String getEventType() {
        return eventType;
    }

    public byte[] getData() {
        return data;
    }

    public byte[] getMetadata() {
        return metadata;
    }

    public Long getVersion() {
        return version;
    }

    public ZonedDateTime getTimestamp() {
        return timestamp;
    }
}
