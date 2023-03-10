package cygni;

import cygni.model.FailResponseDto;
import cygni.model.TicketActivatedEvent;
import cygni.model.TicketCreateEvent;
import cygni.model.TicketOrderEvent;
import cygni.panache.TicketEventDb;
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
    public Uni<Response> findByUserIdAndEventId(
            @QueryParam("eventId") @NotNull String eventId, @QueryParam("userId") @NotNull UUID userId) {
        return service.getTicketsForUser(eventId, userId)
                .onItem().ifNotNull().transform(agg -> Response.ok(agg).build())
                .onItem().ifNull().failWith(() -> new RuntimeException(String.format("Failed to find any tickets for user %s and event %s", userId, eventId)))
                .onFailure().recoverWithItem(Response.status(Response.Status.BAD_REQUEST).entity(FailResponseDto.builder().message(String.format("Failed to find any tickets for user %s and event %s", userId, eventId)).build()).build());
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("create")
    public Uni<Response> create(@NotNull TicketCreateEvent create) {
        return service
                .createTicket(create)
                .onItem().ifNotNull().transform(response -> Response.status(Response.Status.CREATED).entity(response).build())
                .onItem().ifNull().failWith(() -> new RuntimeException(String.format("Failed to create ticket, %s", create)))
                .onFailure().recoverWithItem(Response.status(Response.Status.BAD_REQUEST).entity(FailResponseDto.builder().message(String.format("Failed to create ticket, %s", create)).build()).build());
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("activate")
    public Uni<Response> activate(@NotNull TicketActivatedEvent activateEvent) {
        return service
                .activateTicket(activateEvent)
                .onItem().ifNotNull().transform(res -> Response.status(Response.Status.CREATED).entity(res).build())
                .onItem().ifNull().failWith(() -> new RuntimeException(String.format("Failed to activate ticket, %s", activateEvent)))
                .onFailure().recoverWithItem(Response.status(Response.Status.BAD_REQUEST).entity(FailResponseDto.builder().message(String.format("Failed to activate ticket, %s", activateEvent)).build()).build());
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("order")
    public Uni<Response> order(@NotNull TicketOrderEvent orderEvent) {
        return service
                .orderTicket(orderEvent)
                .onItem().ifNotNull().transform(res -> Response.status(Response.Status.CREATED).entity(res).build())
                .onItem().ifNull().failWith(() -> new RuntimeException(String.format("Failed to order ticket, %s", orderEvent)))
                .onFailure().recoverWithItem(Response.status(Response.Status.BAD_REQUEST).entity(FailResponseDto.builder().message("failed to order tickets").build()).build());
    }

}
