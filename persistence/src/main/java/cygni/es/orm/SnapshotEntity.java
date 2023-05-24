package cygni.es.orm;

import java.time.OffsetDateTime;
import java.util.UUID;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import org.hibernate.annotations.TimeZoneStorage;

@Entity
@Table(name = "snapshots", uniqueConstraints = @UniqueConstraint(columnNames = {"aggregate_id"}))
public class SnapshotEntity {
  @Id @GeneratedValue private UUID id;

  @Column(name = "aggregate_id", nullable = false)
  private UUID aggregateId;

  @Column(name = "aggregate_type", nullable = false)
  private String aggregateType;

  @Column(name = "data")
  private byte[] data;

  @Column(name = "metadata")
  private byte[] metadata;

  @Version
  @Column(name = "version", nullable = false)
  private Long version;

  @Column(name = "timestamp", nullable = false)
  @TimeZoneStorage
  private OffsetDateTime timestamp;

  public SnapshotEntity() {}

  public SnapshotEntity(UUID id, UUID aggregateId, String aggregateType, byte[] data, byte[] metadata, Long version, OffsetDateTime timestamp) {
    this.id = id;
    this.aggregateId = aggregateId;
    this.aggregateType = aggregateType;
    this.data = data;
    this.metadata = metadata;
    this.version = version;
    this.timestamp = timestamp;
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

  public byte[] getData() {
    return data;
  }

  public byte[] getMetadata() {
    return metadata;
  }

  public Long getVersion() {
    return version;
  }

  public OffsetDateTime getTimestamp() {
    return timestamp;
  }
}
