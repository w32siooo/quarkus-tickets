package cygni.experiences.services;

import cygni.experiences.aggregates.ExperienceAggregate;
import io.smallrye.mutiny.Uni;

import java.util.List;

public interface ExperienceQueryService {

     Uni<ExperienceAggregate> getExperience(String aggregateID);

        Uni<List<ExperienceAggregate>> getAllExperiences();
}
