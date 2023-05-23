package cygni.experiences.handlers;

import cygni.es.EventStore;
import cygni.experiences.aggregates.ExperienceAggregate;
import cygni.experiences.dtos.ExperienceAggregateViewDTO;
import cygni.experiences.services.ExperienceQueryService;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import java.util.List;
import java.util.UUID;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class ExperienceQueryHandler implements ExperienceQueryService {

  @Inject EventStore eventStore;

  @Override
  public Uni<ExperienceAggregateViewDTO> getExperience(UUID id) {
    return eventStore.load(id, ExperienceAggregate.class).map(ExperienceAggregate::toDTO);
  }

  @Override
  public Uni<List<ExperienceAggregateViewDTO>> getAllExperiences() {
    return eventStore
        .loadAll(ExperienceAggregate.class, ExperienceAggregate.AGGREGATE_TYPE)
        .flatMap(
            aggregateList ->
                Multi.createFrom()
                    .iterable(aggregateList)
                    .onItem()
                    .transform(ExperienceAggregate::toDTO)
                    .collect()
                    .asList());
  }
}
