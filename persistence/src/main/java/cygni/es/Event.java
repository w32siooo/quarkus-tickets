package cygni.es;

import java.time.OffsetDateTime;
import java.util.UUID;
import org.hibernate.annotations.Type;

public class Event {

  private UUID id;
  @Type(type = "org.hibernate.type.PostgresUUIDType")
  private UUID aggregateId;
  private String type;
  private String aggregateType;
  private long version;
  private byte[] data;
  private byte[] metadata;
  private OffsetDateTime timestamp;

  public Event(String type, String aggregateType) {
    this.id = UUID.randomUUID();
    this.type = type;
    this.aggregateType = aggregateType;
    this.timestamp = OffsetDateTime.now();
  }
public Event(UUID id, UUID aggregateId, String aggregateType, String eventType, byte[] data, byte[] metadata, Long version, OffsetDateTime timestamp) {

    this.id = id;
    this.aggregateId = aggregateId;
    this.aggregateType = aggregateType;
    this.type = eventType;
    this.data = data;
    this.metadata = metadata;
    this.version = version;
    this.timestamp = timestamp;
}

    public Event() {
    }

    public UUID getId() {
        return id;
    }

    public UUID getAggregateId() {
        return aggregateId;
    }

    public String getType() {
        return type;
    }

    public String getAggregateType() {
        return aggregateType;
    }

    public long getVersion() {
        return version;
    }

    public byte[] getData() {
        return data;
    }

    public byte[] getMetadata() {
        return metadata;
    }

    public OffsetDateTime getTimestamp() {
        return timestamp;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public void setAggregateId(UUID aggregateId) {
        this.aggregateId = aggregateId;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setAggregateType(String aggregateType) {
        this.aggregateType = aggregateType;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public void setMetadata(byte[] metadata) {
        this.metadata = metadata;
    }

    public void setTimestamp(OffsetDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
