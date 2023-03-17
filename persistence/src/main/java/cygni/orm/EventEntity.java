package cygni.orm;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Entity;
import java.util.UUID;


import java.time.OffsetDateTime;
import javax.persistence.*;

@Entity
@Table(name = "events",uniqueConstraints = @UniqueConstraint(columnNames = {"aggregate_id", "version"}))
@Getter
@Setter
@Builder
@AllArgsConstructor
public class EventEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "aggregate_id", nullable = false)
    private String aggregateId;

    @Column(name = "aggregate_type", nullable = false)
    private String aggregateType;

    @Column(name = "event_type", nullable = false)
    private String eventType;

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

    public EventEntity() {
    }


}

