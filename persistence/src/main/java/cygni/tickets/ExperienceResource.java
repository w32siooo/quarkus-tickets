package cygni.tickets;

import cygni.legacy.dtos.ChangeExperienceSeatsDTO;
import cygni.tickets.aggregates.ExperienceAggregate;
import cygni.tickets.commands.ChangeExperienceSeatsCommand;
import cygni.tickets.commands.CreateExperienceCommand;
import cygni.tickets.dtos.CreateExperienceRequestDTO;
import cygni.tickets.dtos.ExperienceCreatedDTO;
import cygni.tickets.services.ExperienceCommandService;
import cygni.tickets.services.ExperienceQueryService;
import io.smallrye.mutiny.Uni;
import org.eclipse.microprofile.faulttolerance.CircuitBreaker;
import org.eclipse.microprofile.faulttolerance.Retry;
import org.eclipse.microprofile.faulttolerance.Timeout;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.jboss.logging.Logger;

@Path("/api/v1/tickets")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ExperienceResource {
    private final static Logger logger = Logger.getLogger(ExperienceResource.class);

    @Inject
    ExperienceCommandService commandService;
    @Inject
    ExperienceQueryService queryService;

    @GET
    @Path("{aggregateID}")
    @Retry(maxRetries = 3, delay = 300)
    @Timeout(value = 5000)
    @CircuitBreaker(requestVolumeThreshold = 30, delay = 3000, failureRatio = 0.6)
    public Uni<ExperienceAggregate> getExperience(@PathParam("aggregateID") String aggregateID){
        return queryService.getExperience(aggregateID);
    }


    @POST
    @Retry(maxRetries = 3, delay = 300)
    @Timeout(value = 5000)
    @CircuitBreaker(requestVolumeThreshold = 30, delay = 3000, failureRatio = 0.6)
    public Uni<ExperienceCreatedDTO> createExperience(@Valid CreateExperienceRequestDTO dto){
        final var command = new CreateExperienceCommand(dto.artist(), dto.venue(), dto.date(), dto.price(), dto.seats());
        logger.info("CreateExperienceCommand: " + command);
        return commandService.handle(command);
    }

    @POST
    @Path("{aggregateID}")
    @Retry(maxRetries = 3, delay = 300)
    @Timeout(value = 5000)
    @CircuitBreaker(requestVolumeThreshold = 30, delay = 3000, failureRatio = 0.6)
    public Uni<String> changeExperienceSeats(@PathParam("aggregateID") String aggregateID, @Valid ChangeExperienceSeatsCommand dto ){
       // final var command = new ChangeExperienceSeatsCommand(dto.getNewSeats());
        return commandService.handle(aggregateID,dto).onItem().ifNotNull().transform(s->"changed seats");
    }





}
