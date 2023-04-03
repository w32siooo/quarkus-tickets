package cygni.core.eventsourcing;

import io.smallrye.mutiny.Uni;
import java.util.List;
import java.util.UUID;

public interface EventStoreDB {

    Uni<List<Event>> loadEvents(final UUID aggregateId, long version);

    <T extends AggregateRoot> Uni<Void> persistAndPublish(final T aggregate);


    <T extends AggregateRoot> Uni<T> load(final UUID aggregateId, final Class<T> aggregateType);

    Uni<Boolean> exists(final UUID aggregateId);





}
