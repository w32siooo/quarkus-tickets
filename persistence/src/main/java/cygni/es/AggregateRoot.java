package cygni.es;

import cygni.es.exceptions.InvalidEventException;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Data
@NoArgsConstructor
public abstract class AggregateRoot implements Aggregate {

    protected final List<Event> changes = new ArrayList<>();
    protected UUID id;
    protected String type;
    protected long version;

    public AggregateRoot(final UUID id, final String aggregateType) {
        this.id = id;
        this.type = aggregateType;
    }


    public abstract void when(final Event event);

    public Uni<Void> load(final List<Event> events) {
        return Multi.createFrom().iterable(events).onItem().invoke(event -> {
            this.validateEvent(event);
            this.raiseEvent(event);
            this.version++;
        }).collect().last().replaceWithVoid();

    }

    public void apply(final Event event) {
        this.validateEvent(event);
        event.setAggregateType(this.type);

        when(event);
        changes.add(event);

        this.version++;
        event.setVersion(this.version);
    }

    public void raiseEvent(final Event event) {
        this.validateEvent(event);

        event.setAggregateType(this.type);
        when(event);

        this.version++;
    }

    public void clearChanges() {
        this.changes.clear();
    }

    public void toSnapshot() {
        this.clearChanges();
    }


    private void validateEvent(final Event event) {
        if (Objects.isNull(event) || !event.getAggregateId().equals(this.id))
            throw new InvalidEventException(event.toString());
    }

    protected Event createEvent(String eventType, byte[] data, byte[] metadata) {
        return Event.builder()
                .aggregateId(this.getId())
                .version(this.getVersion())
                .aggregateType(this.getType())
                .eventType(eventType)
                .data(Objects.isNull(data) ? new byte[]{} : data)
                .metadata(Objects.isNull(metadata) ? new byte[]{} : metadata)
                .timestamp(OffsetDateTime.now())
                .build();
    }


    @Override
    public String toString() {
        return String.format("""
                AggregateRoot{
                    id='%s',
                    type='%s',
                    version=%s,
                    changes=%s
                }""", id, type, version, changes.size());


    }
}
