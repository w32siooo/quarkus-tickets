package cygni.experiences.handlers;

import cygni.es.EventStoreDB;
import cygni.es.dto.RequestAcceptedDTO;
import cygni.experiences.aggregates.ExperienceAggregate;
import cygni.experiences.commands.BookExperienceCommand;
import cygni.experiences.commands.CancelExperienceCommand;
import cygni.experiences.commands.ChangeExperienceSeatsCommand;
import cygni.experiences.commands.CreateExperienceCommand;
import cygni.experiences.dtos.ExperienceCreatedDTO;
import cygni.experiences.services.ExperienceCommandService;
import io.smallrye.mutiny.Uni;
import java.util.UUID;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import org.jboss.logging.Logger;

@ApplicationScoped
public class ExperienceCommandHandler implements ExperienceCommandService {

  private static final Logger logger = Logger.getLogger(ExperienceCommandHandler.class);
  @Inject EventStoreDB eventStoreDB;

  @Override
  public Uni<ExperienceCreatedDTO> handle(CreateExperienceCommand cmd) {
    final var aggregate = new ExperienceAggregate(UUID.randomUUID());
    aggregate.createExperience(cmd.artist(), cmd.venue(), cmd.date(), cmd.price(), cmd.seats());
    logger.infof("created experience: %s", aggregate);
    return eventStoreDB
        .persistAndPublish(aggregate)
        .replaceWith(
            new ExperienceCreatedDTO(
                aggregate.getArtist(),
                aggregate.getVenue(),
                aggregate.getDate(),
                aggregate.getPrice(),
                aggregate.getTotalSeats(),
                aggregate.getId().toString()))
        .onItem()
        .invoke(() -> logger.infof("created experience: %s", aggregate));
  }

  @Override
  public Uni<RequestAcceptedDTO> handle(UUID aggregateID, ChangeExperienceSeatsCommand cmd) {
    logger.error("change seats command: " + cmd.newSeats());
    return eventStoreDB
        .load(aggregateID, ExperienceAggregate.class)
        .onItem()
        .transform(
            agg -> {
              agg.changeTotalSeats(cmd.newSeats());
              return agg;
            })
        .chain(agg -> eventStoreDB.persistAndPublish(agg))
        .map(agg -> new RequestAcceptedDTO("total seats changed", aggregateID));
  }

  @Override
  public Uni<RequestAcceptedDTO> handle(UUID aggregateID, CancelExperienceCommand cmd) {
    logger.error("cancel command: " + cmd);
    return eventStoreDB
        .load(aggregateID, ExperienceAggregate.class)
        .onItem()
        .transform(
            agg -> {
              agg.cancelExperience(cmd.reason());
              return agg;
            })
        .chain(agg -> eventStoreDB.persistAndPublish(agg))
        .map(agg -> new RequestAcceptedDTO("experience cancelled", aggregateID));
  }

  @Override
  public Uni<RequestAcceptedDTO> handle(UUID aggregateID, BookExperienceCommand cmd) {
    return eventStoreDB
        .load(aggregateID, ExperienceAggregate.class)
        .onItem()
        .transform(
            agg -> {
              agg.bookExperience(cmd.userID(), cmd.seats());
              return agg;
            })
        .chain(agg -> eventStoreDB.persistAndPublish(agg))
        .map(agg -> new RequestAcceptedDTO("experience booked", aggregateID));
  }
}
