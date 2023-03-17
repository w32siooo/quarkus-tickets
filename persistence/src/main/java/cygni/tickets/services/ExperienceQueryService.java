package cygni.tickets.services;

import cygni.tickets.aggregates.ExperienceAggregate;
import io.smallrye.mutiny.Uni;

public interface ExperienceQueryService {

     Uni<ExperienceAggregate> getExperience(String aggregateID);
}
