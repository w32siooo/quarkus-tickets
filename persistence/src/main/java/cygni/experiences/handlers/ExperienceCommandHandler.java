package cygni.experiences.handlers;

import cygni.es.EventStoreDB;
import cygni.experiences.aggregates.ExperienceAggregate;
import cygni.experiences.commands.BookExperienceCommand;
import cygni.experiences.commands.CancelExperienceCommand;
import cygni.experiences.commands.ChangeExperienceSeatsCommand;
import cygni.experiences.commands.CreateExperienceCommand;
import cygni.experiences.dtos.ExperienceCreatedDTO;
import cygni.experiences.services.ExperienceCommandService;
import io.smallrye.mutiny.Uni;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.UUID;
@ApplicationScoped
public class ExperienceCommandHandler implements ExperienceCommandService {

    private final static Logger logger = Logger.getLogger(ExperienceCommandHandler.class);
    @Inject
    EventStoreDB eventStoreDB;


    @Override
    public Uni<ExperienceCreatedDTO> handle(CreateExperienceCommand cmd) {
        final var aggregate = new ExperienceAggregate(UUID.randomUUID());
        aggregate.createExperience(cmd.artist(), cmd.venue(), cmd.date(), cmd.price(), cmd.seats());
        logger.infof("created experience: %s", aggregate);
        return eventStoreDB.persistAndPublish(aggregate).replaceWith(new ExperienceCreatedDTO(aggregate.getArtist(), aggregate.getVenue(),
                aggregate.getDate(), aggregate.getPrice(), aggregate.getTotalSeats(), aggregate.getId().toString()))
                .onItem().invoke(() -> logger.infof("created experience: %s", aggregate));
    }

    @Override
    public Uni<Void> handle(UUID aggregateID, ChangeExperienceSeatsCommand cmd) {
        logger.error("change seats command: " + cmd.newSeats());
        return eventStoreDB.load(aggregateID, ExperienceAggregate.class)
                .onItem().transform(agg->{
                    agg.changeTotalSeats(cmd.newSeats());
                    return agg;
                }).chain(agg->eventStoreDB.persistAndPublish(agg));
    }

    @Override
    public Uni<Void> handle(UUID aggregateID, CancelExperienceCommand cmd) {
        logger.error("cancel command: " + cmd);
        return eventStoreDB.load(aggregateID, ExperienceAggregate.class)
                .onItem().transform(agg->{
                    agg.cancelExperience(cmd.reason());
                    return agg;
                }).chain(agg->eventStoreDB.persistAndPublish(agg));
    }

    @Override
    public Uni<Void> handle(UUID aggregateID, BookExperienceCommand cmd) {
        return eventStoreDB.load(aggregateID, ExperienceAggregate.class)
                .onItem().transform(agg->{
                    agg.bookExperience(cmd.userID(),cmd.seats());
                    return agg;
                }).chain(agg->eventStoreDB.persistAndPublish(agg));
    }


}
