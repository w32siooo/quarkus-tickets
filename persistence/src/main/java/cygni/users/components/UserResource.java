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
    public Uni<Void> createNewUser(@Valid CreateNewUserDTO dto){

        return commandService.handle(dto);
    }

    @POST
    @Path("{userId}/experiences/{experienceId}/add")
    public Uni<Void> addExperience(@NotNull @PathParam("userId") UUID userId, @NotNull @PathParam("experienceId") UUID experienceId){
        BuyTicketDTO dto = new BuyTicketDTO(experienceId, 1, 1L);
        return commandService.handle(userId,dto);
    }

    @POST
    @Path("{userId}/experiences/{experienceId}/remove")
    public Uni<Void> removeExperience(@NotNull @PathParam("userId") UUID userId, @NotNull @PathParam("experienceId")  UUID experienceId){
        var dto = new RemoveTicketDTO(experienceId);
        return commandService.handle(userId,dto);
    }

    @POST
    @Path("{userId}/deposit/{toAdd}")
    public Uni<Void> depositBalance(@NotNull @PathParam("userId") UUID userId, @NotNull @PathParam("toAdd") Long toAdd){
        return commandService.handle(userId, toAdd);
    }

    @GET
    @Path("{userId}")
    public Uni<UserViewDTO> getUser(@NotNull @PathParam("userId") UUID userId){
        return queryService.handle(userId);
    }
}
