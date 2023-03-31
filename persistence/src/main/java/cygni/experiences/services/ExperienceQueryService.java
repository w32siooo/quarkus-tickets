package cygni.experiences.services;

import cygni.experiences.aggregates.ExperienceAggregate;
import cygni.experiences.dtos.ExperienceAggregateViewDTO;
import io.smallrye.mutiny.Uni;

import java.util.List;
import java.util.UUID;

public interface ExperienceQueryService {

    Uni<ExperienceAggregateViewDTO> getExperience(UUID aggregateID);

    Uni<List<ExperienceAggregateViewDTO>> getAllExperiences();
}
