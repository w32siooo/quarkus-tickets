package cygni.es.orm;

import java.time.OffsetDateTime;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.persistence.Version;

@Entity
@Table(name = "snapshots", uniqueConstraints = @UniqueConstraint(columnNames = {"aggregate_id"}))
public class SnapshotEntity {
  @Id @GeneratedValue private UUID id;

  @Column(name = "aggregate_id", nullable = false)
  private UUID aggregateId;

  @Column(name = "aggregate_type", nullable = false)
  private String aggregateType;

  @Lob
  @Column(name = "data")
  private byte[] data;

  @Lob
  @Column(name = "metadata")
  private byte[] metadata;

  @Version
  @Column(name = "version", nullable = false)
  private Long version;

  @Column(name = "timestamp", nullable = false)
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
