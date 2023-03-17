package cygni.tickets.services;

import cygni.es.EventStoreDB;
import cygni.tickets.aggregates.ExperienceAggregate;
import cygni.tickets.commands.ChangeExperienceSeatsCommand;
import cygni.tickets.commands.CreateExperienceCommand;
import cygni.tickets.dtos.ExperienceCreatedDTO;
import io.smallrye.mutiny.Uni;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.ws.rs.PathParam;
import java.util.UUID;
@ApplicationScoped
public class ExperienceCommandHandler implements ExperienceCommandService{

    private final static Logger logger = Logger.getLogger(ExperienceCommandHandler.class);
    @Inject
    EventStoreDB eventStoreDB;
    @Override
    public Uni<ExperienceCreatedDTO> handle(CreateExperienceCommand cmd) {
        final var aggregate = new ExperienceAggregate(UUID.randomUUID().toString());
        aggregate.createExperience(cmd.artist(), cmd.venue(), cmd.date(), cmd.price(), cmd.seats());
        logger.infof("created experience: %s", aggregate);
        return eventStoreDB.persistAndPublish(aggregate).replaceWith(new ExperienceCreatedDTO(aggregate.getArtist(), aggregate.getVenue(),
                aggregate.getDate(), aggregate.getPrice(), aggregate.getSeats()))
                .onItem().invoke(() -> logger.infof("created experience: %s", aggregate));
    }

    @Override
    public Uni<Void> handle(String aggregateID, ChangeExperienceSeatsCommand cmd) {
        logger.error("change seats command: " + cmd.newSeats());
        return eventStoreDB.load(aggregateID, ExperienceAggregate.class)
                .onItem().transform(agg->{
                    agg.changeSeats(cmd.newSeats());
                    return agg;
                }).chain(agg->eventStoreDB.persistAndPublish(agg));
    }



}
