package cygni;

import cygni.model.TicketAggregate;
import cygni.model.TicketCreateEvent;
import cygni.services.TicketService;
import io.smallrye.mutiny.Uni;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.UUID;

@Path("tickets")
@Slf4j
public class TicketResource {
    @Inject
    TicketService service;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<TicketAggregate> findByUserIdAndEventId(@QueryParam("eventId") @NotNull String eventId,
                                                       @QueryParam("userId") @NotNull UUID userId) {
        return service.getTicketsForUser(eventId, userId);
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<Response> create(@NotNull TicketCreateEvent create) {
        return service.createTicket(create)
                .map(response-> Response.status(Response.Status.CREATED)
                        .entity(response).build());
    }
}