package cygni.users.components;

import cygni.es.dto.RequestFailedDTO;
import cygni.es.exceptions.AggregateNotFoundException;
import cygni.es.exceptions.BookingFailedException;
import cygni.es.mappers.EventSourcingMappers;
import cygni.users.commands.BuyTicketCommand;
import cygni.users.commands.CreateNewUserCommand;
import cygni.users.dtos.BuyTicketDTO;
import cygni.users.dtos.RemoveTicketDTO;
import io.quarkus.security.Authenticated;
import io.smallrye.mutiny.Uni;
import org.slf4j.Logger;

import java.util.UUID;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

@Path("api/v1/users")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Authenticated
public class UserResource {

  private static final Logger log = org.slf4j.LoggerFactory.getLogger(UserResource.class);

  @Inject UserCommandService commandService;

  @Inject UserQueryService queryService;

  @POST
  @Path("create")
  public Uni<Response> createNewUser(@Context SecurityContext ctx) {
    var cmd =
        new CreateNewUserCommand(
            ctx.getUserPrincipal().getName(),
            999L,
            EventSourcingMappers.uuidFromSecurityContext(ctx));
    log.info("Received command: {}", cmd);
    return commandService.handle(cmd).map(s -> Response.status(201).entity(s).build());
  }

  @POST
  @Path("/buyExperience")
  public Uni<Response> bookExperience(
      @NotNull @Valid BuyTicketCommand cmd, @Context SecurityContext ctx) {
    BuyTicketDTO dto = new BuyTicketDTO(cmd.experienceID(), cmd.seats());
    log.info("Received command: {}", cmd);

    return commandService
        .handle(EventSourcingMappers.uuidFromSecurityContext(ctx), dto)
        .onItem()
        .ifNotNull()
        .transform(s -> Response.status(201).entity(s).build())
        .onFailure(AggregateNotFoundException.class)
        .recoverWithItem(
            s -> Response.status(404).entity(new RequestFailedDTO(s.getMessage())).build())
        .onFailure(BookingFailedException.class)
        .recoverWithItem(
            s -> Response.status(400).entity(new RequestFailedDTO(s.getMessage())).build())
        .onFailure()
        .recoverWithItem(
            s -> Response.status(500).entity(new RequestFailedDTO(s.getMessage())).build());
  }

  @POST
  @Path("experiences/{experienceId}/remove")
  public Uni<Response> removeExperience(
      @Context SecurityContext ctx, @NotNull @PathParam("experienceId") UUID experienceId) {

    var dto = new RemoveTicketDTO(experienceId);
    return commandService
        .handle(EventSourcingMappers.uuidFromSecurityContext(ctx), dto)
        .map(s -> Response.status(201).entity(s).build());
  }

  @POST
  @Path("deposit/{toAdd}")
  public Uni<Response> depositBalance(
      @Context SecurityContext ctx, @NotNull @PathParam("toAdd") Long toAdd) {
    return commandService
        .handle(EventSourcingMappers.uuidFromSecurityContext(ctx), toAdd)
        .map(s -> Response.status(201).entity(s).build());
  }

  @GET
  @Path("")
  public Uni<Response> getUser(@Context SecurityContext ctx) {
    return queryService
        .handle(EventSourcingMappers.uuidFromSecurityContext(ctx))
        .map(s -> Response.status(200).entity(s).build());
  }
}
