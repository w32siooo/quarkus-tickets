package cygni.orm;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.persistence.Version;
import java.time.OffsetDateTime;
import java.util.UUID;


@Entity
@Getter
@Builder
@AllArgsConstructor
@Table(name = "snapshots", uniqueConstraints = @UniqueConstraint(columnNames = {"aggregate_id"}))
public class SnapshotEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;
    @Column(name = "aggregate_id", nullable = false)
    private String aggregateId;
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

    public SnapshotEntity() {
    }

}
