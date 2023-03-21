package cygni.experiences.services;

import cygni.experiences.aggregates.ExperienceAggregate;
import io.smallrye.mutiny.Uni;

public interface ExperienceQueryService {

     Uni<ExperienceAggregate> getExperience(String aggregateID);
}
