package cygni.experiences.services;

import cygni.experiences.aggregates.ExperienceAggregate;
import io.smallrye.mutiny.Uni;

import java.util.List;
import java.util.UUID;

public interface ExperienceQueryService {

     Uni<ExperienceAggregate> getExperience(UUID aggregateID);

        Uni<List<ExperienceAggregate>> getAllExperiences();
}
