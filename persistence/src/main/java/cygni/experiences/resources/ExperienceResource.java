package cygni.experiences.resources;

import cygni.experiences.commands.CancelExperienceCommand;
import cygni.experiences.commands.ChangeExperienceSeatsCommand;
import cygni.experiences.commands.CreateExperienceCommand;
import cygni.experiences.dtos.CancelExperienceDTO;
import cygni.experiences.dtos.ChangeExperienceSeatsDTO;
import cygni.experiences.dtos.CreateExperienceRequestDTO;
import cygni.experiences.services.ExperienceCommandService;
import cygni.experiences.services.ExperienceQueryService;
import io.smallrye.mutiny.Uni;
import java.util.UUID;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.eclipse.microprofile.faulttolerance.CircuitBreaker;
import org.eclipse.microprofile.faulttolerance.Retry;
import org.eclipse.microprofile.faulttolerance.Timeout;
import org.jboss.logging.Logger;

@Path("/api/v1/tickets")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ExperienceResource {
  private static final Logger logger = Logger.getLogger(ExperienceResource.class);

  @Inject ExperienceCommandService commandService;
  @Inject ExperienceQueryService queryService;

  @GET
  @Path("{aggregateID}")
  @Retry(maxRetries = 3, delay = 300)
  @Timeout(value = 5000)
  @CircuitBreaker(requestVolumeThreshold = 30, delay = 3000, failureRatio = 0.6)
  public Uni<Response> getExperience(@PathParam("aggregateID") UUID aggregateID) {
    return queryService.getExperience(aggregateID).map(s -> Response.status(200).entity(s).build());
  }

  /**
   * @return all experiences, this is an expensive query as it essentially calls getExperience n
   *     times.
   */
  @GET
  @Path("")
  @Retry(maxRetries = 3, delay = 300)
  @Timeout(value = 5000)
  @CircuitBreaker(requestVolumeThreshold = 30, delay = 3000, failureRatio = 0.6)
  public Uni<Response> getAllExperiences() {
    return queryService.getAllExperiences().map(s -> Response.status(200).entity(s).build());
  }

  /**
   * Create experience.
   *
   * @param dto
   * @return status code and response dto.
   */
  @POST
  @Retry(maxRetries = 3, delay = 300)
  @Timeout(value = 5000)
  @CircuitBreaker(requestVolumeThreshold = 30, delay = 3000, failureRatio = 0.6)
  public Uni<Response> createExperience(@Valid CreateExperienceRequestDTO dto) {
    final var command =
        new CreateExperienceCommand(
            dto.artist(), dto.venue(), dto.date(), dto.price(), dto.seats());
    logger.info("CreateExperienceCommand: " + command);
    return commandService.handle(command).map(s -> Response.status(201).entity(s).build());
  }

  @POST
  @Path("{aggregateID}/changeSeats")
  @Retry(maxRetries = 3, delay = 300)
  @Timeout(value = 5000)
  @CircuitBreaker(requestVolumeThreshold = 30, delay = 3000, failureRatio = 0.6)
  public Uni<Response> changeExperienceSeats(
      @PathParam("aggregateID") UUID aggregateID, @Valid ChangeExperienceSeatsDTO dto) {
    final var command = new ChangeExperienceSeatsCommand(dto.newSeats());
    return commandService
        .handle(aggregateID, command)
        .map(s -> Response.status(202).entity(s).build());
  }

  @POST
  @Path("{aggregateID}/cancel")
  @Retry(maxRetries = 3, delay = 300)
  @Timeout(value = 5000)
  @CircuitBreaker(requestVolumeThreshold = 30, delay = 3000, failureRatio = 0.6)
  public Uni<Response> cancelExperience(
      @PathParam("aggregateID") UUID aggregateID, @Valid CancelExperienceDTO dto) {
    final var command = new CancelExperienceCommand(aggregateID, dto.reason());
    return commandService
        .handle(aggregateID, command)
        .map(s -> Response.status(202).entity(s).build());
  }
}
