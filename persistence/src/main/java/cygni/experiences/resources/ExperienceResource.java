package cygni.experiences.resources;

import cygni.experiences.commands.CancelExperienceCommand;
import cygni.experiences.commands.ChangeExperienceSeatsCommand;
import cygni.experiences.commands.CreateExperienceCommand;
import cygni.experiences.dtos.CancelExperienceDTO;
import cygni.experiences.dtos.ChangeExperienceSeatsDTO;
import cygni.experiences.dtos.CreateExperienceRequestDTO;
import cygni.experiences.services.ExperienceCommandService;
import cygni.experiences.services.ExperienceQueryService;
import io.quarkus.security.Authenticated;
import io.smallrye.mutiny.Uni;
import java.util.UUID;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.eclipse.microprofile.faulttolerance.CircuitBreaker;
import org.eclipse.microprofile.faulttolerance.Retry;
import org.eclipse.microprofile.faulttolerance.Timeout;
import org.jboss.logging.Logger;

@Path("/api/v1/tickets")
@Authenticated
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
  @RolesAllowed({"system", "admin"})
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
  @RolesAllowed({"system", "admin"})
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
  @RolesAllowed({"system", "admin"})
  public Uni<Response> createExperience(@Valid CreateExperienceRequestDTO dto) {

    return Uni.createFrom()
        .item(
            new CreateExperienceCommand(
                dto.artist(), dto.venue(), dto.date(), dto.price(), dto.seats()))
        .log()
        .flatMap(
            cmd -> commandService.handle(cmd).map(s -> Response.status(201).entity(s).build()));
  }

  @POST
  @Path("{aggregateID}/changeSeats")
  @Retry(maxRetries = 3, delay = 300)
  @Timeout(value = 5000)
  @CircuitBreaker(requestVolumeThreshold = 30, delay = 3000, failureRatio = 0.6)
  @RolesAllowed({"system", "admin"})
  public Uni<Response> changeExperienceSeats(
      @PathParam("aggregateID") UUID aggregateID, @Valid ChangeExperienceSeatsDTO dto) {
    return Uni.createFrom()
        .item(new ChangeExperienceSeatsCommand(dto.newSeats()))
        .log()
        .flatMap(
            cmd ->
                commandService
                    .handle(aggregateID, cmd)
                    .map(s -> Response.status(202).entity(s).build()));
  }

  @POST
  @Path("{aggregateID}/cancel")
  @Retry(maxRetries = 3, delay = 300)
  @Timeout(value = 5000)
  @CircuitBreaker(requestVolumeThreshold = 30, delay = 3000, failureRatio = 0.6)
  @RolesAllowed({"system", "admin"})
  public Uni<Response> cancelExperience(
      @PathParam("aggregateID") UUID aggregateID, @Valid CancelExperienceDTO dto) {
    final var command = new CancelExperienceCommand(aggregateID, dto.reason());
    return Uni.createFrom()
        .item(new CancelExperienceCommand(aggregateID, dto.reason()))
        .log()
        .flatMap(
            cmd ->
                commandService
                    .handle(aggregateID, cmd)
                    .map(s -> Response.status(202).entity(s).build()));
  }
}
