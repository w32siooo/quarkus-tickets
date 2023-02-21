package org.acme.resources;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import org.acme.hibernate.orm.panache.TicketEventDb;
import org.acme.model.TicketAggregate;
import org.acme.services.TicketService;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;
import java.util.UUID;

@Path("api/tickets")
public class TicketResource {
    /**
     * Endpoint to generate a new quote request id and send it to "quote-requests" channel (which
     * maps to the "quote-requests" RabbitMQ exchange) using the emitter.
     */

    @Inject
    TicketService ticketService;

    @GET
    @Path("")
    @Produces(MediaType.APPLICATION_JSON)
    public List<PanacheEntityBase> list() {

        return TicketEventDb.listAll();
    }

    @GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)

    public TicketAggregate getTicketsForUser(@PathParam("id") String id, @QueryParam("userId") UUID userId){
        return ticketService.getTicketsForUser(id,userId);
    }
}
