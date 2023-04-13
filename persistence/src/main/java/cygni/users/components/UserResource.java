package cygni.users.components;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import cygni.es.dto.RequestFailedDTO;
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
            ctx.getUserPrincipal().getName(), 999L, EventSourcingMappers.uuidFromSecurityContext(ctx));
    return commandService.handle(cmd).map(s -> Response.status(201).entity(s).build());
  }

  @POST
  @Path("{userId}/buyExperience")
  public Uni<Response> buyExperience(
      @NotNull @PathParam("userId") UUID userId, @NotNull @Valid BuyTicketCommand cmd) {
    BuyTicketDTO dto = new BuyTicketDTO(cmd.experienceID(), cmd.seats());
    return commandService
        .handle(userId, dto)
        .onItem()
        .ifNotNull()
        .transform(s -> Response.status(201).entity(s).build())
        .onFailure()
        .recoverWithUni(
            s ->
                Uni.createFrom()
                    .item(
                        Response.status(400).entity(new RequestFailedDTO(s.getMessage())).build()));
  }

  @POST
  @Path("{userId}/experiences/{experienceId}/remove")
  public Uni<Response> removeExperience(
      @NotNull @PathParam("userId") UUID userId,
      @NotNull @PathParam("experienceId") UUID experienceId) {
    var dto = new RemoveTicketDTO(experienceId);
    return commandService.handle(userId, dto).map(s -> Response.status(201).entity(s).build());
  }

  @POST
  @Path("{userId}/deposit/{toAdd}")
  public Uni<Response> depositBalance(
      @NotNull @PathParam("userId") UUID userId, @NotNull @PathParam("toAdd") Long toAdd) {
    return commandService.handle(userId, toAdd).map(s -> Response.status(201).entity(s).build());
  }

  @GET
  @Path("{userId}")
  public Uni<Response> getUser(@NotNull @PathParam("userId") UUID userId) {
    return queryService.handle(userId).map(s -> Response.status(200).entity(s).build());
  }
}
