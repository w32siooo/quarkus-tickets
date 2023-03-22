package cygni.experiences.handlers;

import cygni.es.EventStore;
import cygni.experiences.aggregates.ExperienceAggregate;
import cygni.experiences.services.ExperienceQueryService;
import io.smallrye.mutiny.Uni;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.List;

@ApplicationScoped
public class ExperienceQueryHandler implements ExperienceQueryService {

    @Inject
    EventStore eventStore;

    @Override
    public Uni<ExperienceAggregate> getExperience(String id) {
        return eventStore.load(id ,ExperienceAggregate.class);
    }

    @Override
    public Uni<List<ExperienceAggregate>> getAllExperiences() {
        return eventStore.loadAll(ExperienceAggregate.class, ExperienceAggregate.AGGREGATE_TYPE);
    }
}
