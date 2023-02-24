package cygni;

import cygni.model.TicketCreateEvent;
import cygni.panache.TicketEventDb;
import cygni.services.TicketService;
import io.smallrye.mutiny.Uni;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.UUID;

@Path("/hello")
public class GreetingResource {
    @Inject
    TicketService service;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<List<TicketEventDb>> hello() {
        return service.getTicketsForUser("ayos", UUID.fromString("d542764a-6583-4f73-92d9-c214078aac56"));
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<Response> create(TicketCreateEvent create) {
        return service.createTicket(create)
                .map(s-> Response.status(Response.Status.CREATED).entity(s).build());
    }
}