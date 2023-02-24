package cygni;

import cygni.model.TicketAggregate;
import cygni.model.TicketCreateEvent;
import cygni.panache.TicketEventDb;
import cygni.services.TicketService;
import io.smallrye.mutiny.Uni;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.UUID;

@Path("/hello")
@Slf4j
public class GreetingResource {
    @Inject
    TicketService service;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<TicketAggregate> findByUserIdAndEventId(@QueryParam("eventId") String eventId, @QueryParam("userId") UUID userId) {
        return service.getTicketsForUser(eventId, userId);
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<Response> create(TicketCreateEvent create) {
        return service.createTicket(create)
                .map(s-> Response.status(Response.Status.CREATED).entity(s).build());
    }
}