package cygni.es;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Snapshot {

    private UUID id;

    private String aggregateId;


    private String aggregateType;

    private byte[] data;

    private byte[] metaData;


    private long version;
    private OffsetDateTime timestamp;

}
