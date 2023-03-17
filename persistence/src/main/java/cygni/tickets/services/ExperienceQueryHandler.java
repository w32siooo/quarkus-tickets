package cygni.tickets.services;

import cygni.es.EventStore;
import cygni.tickets.aggregates.ExperienceAggregate;
import io.smallrye.mutiny.Uni;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class ExperienceQueryHandler implements ExperienceQueryService {

    @Inject
    EventStore eventStore;

    @Override
    public Uni<ExperienceAggregate> getExperience(String id) {
        return eventStore.load(id ,ExperienceAggregate.class);
    }
}
