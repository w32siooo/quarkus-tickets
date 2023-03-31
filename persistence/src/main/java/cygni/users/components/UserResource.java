package cygni.users.components;

import cygni.users.dtos.BuyTicketDTO;
import cygni.users.dtos.CreateNewUserDTO;
import cygni.users.dtos.RemoveTicketDTO;
import cygni.users.dtos.UserViewDTO;
import io.smallrye.mutiny.Uni;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.UUID;

@Path("api/v1/users")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class UserResource {

    @Inject
    UserCommandService commandService;

    @Inject
    UserQueryService queryService;

    @POST
    @Path("create")
    public Uni<Response> createNewUser(@Valid CreateNewUserDTO dto){

        return commandService.handle(dto).map(s->Response.status(201).entity(s).build());
    }

    @POST
    @Path("{userId}/experiences/{experienceId}/add")
    public Uni<Response> addExperience(@NotNull @PathParam("userId") UUID userId, @NotNull @PathParam("experienceId") UUID experienceId){
        BuyTicketDTO dto = new BuyTicketDTO(experienceId, 1, 1L);
        return commandService.handle(userId,dto).map(s->Response.status(201).entity(s).build());
    }

    @POST
    @Path("{userId}/experiences/{experienceId}/remove")
    public Uni<Response> removeExperience(@NotNull @PathParam("userId") UUID userId, @NotNull @PathParam("experienceId")  UUID experienceId){
        var dto = new RemoveTicketDTO(experienceId);
        return commandService.handle(userId,dto).map(s->Response.status(201).entity(s).build());
    }

    @POST
    @Path("{userId}/deposit/{toAdd}")
    public Uni<Response> depositBalance(@NotNull @PathParam("userId") UUID userId, @NotNull @PathParam("toAdd") Long toAdd){
        return commandService.handle(userId, toAdd).map(s->Response.status(201).entity(s).build());
    }

    @GET
    @Path("{userId}")
    public Uni<Response> getUser(@NotNull @PathParam("userId") UUID userId){
        return queryService.handle(userId).map(s->Response.status(200).entity(s).build());
    }
}
