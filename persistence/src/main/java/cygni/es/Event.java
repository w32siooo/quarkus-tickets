package cygni.es;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;

import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Event {

    public Event(String eventType, String aggregateType) {
        this.id = UUID.randomUUID();
        this.eventType = eventType;
        this.aggregateType = aggregateType;
        this.timestamp = OffsetDateTime.now();
    }

    private UUID id;
    @Type(type="pg-uuid")
    private UUID aggregateId;

    private String eventType;

    private String aggregateType;

    private long version;

    private byte[] data;

    private byte[] metadata;

    private OffsetDateTime timestamp;

}