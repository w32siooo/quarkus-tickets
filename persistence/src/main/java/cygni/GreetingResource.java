package cygni;

import cygni.model.TicketCreateEvent;
import cygni.panache.TicketEventDb;
import cygni.services.TicketService;
import io.smallrye.mutiny.Uni;

import javax.inject.Inject;
import javax.print.attribute.standard.Media;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/hello")
public class GreetingResource {
    @Inject
    TicketService service;

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String hello() {
        return "Hello from RESTEasy Reactive";
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<Response> create(TicketCreateEvent create) {
        return service.createTicket(create)
                .map(s-> Response.status(Response.Status.CREATED).entity(s).build());
    }
}