package cygni.es;

import io.smallrye.mutiny.Uni;

import java.util.List;

public interface EventStoreDB {

    Uni<List<Event>> loadEvents(final String aggregateId, long version);

    <T extends AggregateRoot> Uni<Void> persistAndPublish(final T aggregate);


    <T extends AggregateRoot> Uni<T> load(final String aggregateId, final Class<T> aggregateType);

    Uni<Boolean> exists(final String aggregateId);
}
