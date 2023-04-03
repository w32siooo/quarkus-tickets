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
public class Snapshot {

    private UUID id;
    @Type(type="org.hibernate.type.PostgresUUIDType")
    private UUID aggregateId;


    private String aggregateType;

    private byte[] data;

    private byte[] metaData;


    private long version;
    private OffsetDateTime timestamp;

}