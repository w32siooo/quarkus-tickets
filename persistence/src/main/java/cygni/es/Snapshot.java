package cygni.es;

import java.time.OffsetDateTime;
import java.util.UUID;
import org.hibernate.annotations.Type;

public class Snapshot {

    private UUID id;
    @Type(type="org.hibernate.type.PostgresUUIDType")
    private UUID aggregateId;


    private String aggregateType;

    private byte[] data;

    private byte[] metaData;


    private long version;
    private OffsetDateTime timestamp;

    public Snapshot(UUID id, UUID aggregateId, String aggregateType, byte[] data, byte[] metaData, long version, OffsetDateTime timestamp) {
        this.id = id;
        this.aggregateId = aggregateId;
        this.aggregateType = aggregateType;
        this.data = data;
        this.metaData = metaData;
        this.version = version;
        this.timestamp = timestamp;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getAggregateId() {
        return aggregateId;
    }

    public void setAggregateId(UUID aggregateId) {
        this.aggregateId = aggregateId;
    }

    public String getAggregateType() {
        return aggregateType;
    }

    public void setAggregateType(String aggregateType) {
        this.aggregateType = aggregateType;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public byte[] getMetaData() {
        return metaData;
    }

    public void setMetaData(byte[] metaData) {
        this.metaData = metaData;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public OffsetDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(OffsetDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
