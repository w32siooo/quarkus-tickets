package cygni.legacy.resources;

import cygni.legacy.dtos.FailResponseDto;
import cygni.legacy.events.TicketActivateEvent;
import cygni.legacy.events.TicketCreateEvent;
import cygni.legacy.events.TicketOrderEvent;
import cygni.legacy.services.TicketService;
import io.smallrye.mutiny.Uni;
import java.util.UUID;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;

@Path("api/tickets")
@Slf4j
public class TicketResource {
  @Inject TicketService service;

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Path("user")
  public Uni<Response> queryByUserIdAndEventId(
      @QueryParam("eventId") @NotNull UUID eventId, @QueryParam("userId") @NotNull UUID userId) {
    return service
        .queryByUserIdAndEventId(eventId, userId)
        .onItem()
        .ifNotNull()
        .transform(agg -> Response.ok(agg).build())
        .onItem()
        .ifNull()
        .failWith(
            () ->
                new RuntimeException(
                    String.format(
                        "Failed to find any tickets for user %s and event %s", userId, eventId)))
        .onFailure()
        .recoverWithItem(
            Response.status(Response.Status.BAD_REQUEST)
                .entity(
                    FailResponseDto.builder()
                        .message(
                            String.format(
                                "Failed to find any tickets for user %s and event %s",
                                userId, eventId))
                        .build())
                .build());
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Path("event")
  public Uni<Response> queryByEventId(@QueryParam("eventId") @NotNull UUID eventId) {
    return service
        .queryByEventId(eventId)
        .onItem()
        .ifNotNull()
        .transform(agg -> Response.ok(agg).build())
        .onItem()
        .ifNull()
        .failWith(
            () ->
                new RuntimeException(
                    String.format("Failed to find any tickets for event %s", eventId)))
        .onFailure()
        .recoverWithItem(
            Response.status(Response.Status.BAD_REQUEST)
                .entity(
                    FailResponseDto.builder()
                        .message(String.format("Failed to find any tickets for  event %s", eventId))
                        .build())
                .build());
  }

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  @Path("create")
  public Uni<Response> create(@NotNull @Valid TicketCreateEvent create) {
    return service
        .createTicket(create)
        .onItem()
        .ifNotNull()
        .transform(response -> Response.status(Response.Status.CREATED).entity(response).build())
        .onItem()
        .ifNull()
        .failWith(() -> new RuntimeException(String.format("Failed to create ticket, %s", create)))
        .onFailure()
        .recoverWithItem(
            Response.status(Response.Status.BAD_REQUEST)
                .entity(
                    FailResponseDto.builder()
                        .message(String.format("Failed to create ticket, %s", create))
                        .build())
                .build());
  }

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  @Path("order")
  public Uni<Response> order(@NotNull @Valid TicketOrderEvent orderEvent) {
    return service
        .orderTicket(orderEvent)
        .onItem()
        .ifNotNull()
        .transform(res -> Response.status(Response.Status.CREATED).entity(res).build())
        .onItem()
        .ifNull()
        .failWith(
            () -> new RuntimeException(String.format("Failed to order ticket, %s", orderEvent)))
        .onFailure()
        .recoverWithItem(
            Response.status(Response.Status.BAD_REQUEST)
                .entity(FailResponseDto.builder().message("failed to order tickets").build())
                .build());
  }

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  @Path("activate")
  public Uni<Response> activate(@NotNull @Valid TicketActivateEvent activateEvent) {
    return service
        .activateTicket(activateEvent)
        .onItem()
        .ifNotNull()
        .transform(res -> Response.status(Response.Status.CREATED).entity(res).build())
        .onItem()
        .ifNull()
        .failWith(
            () ->
                new RuntimeException(String.format("Failed to activate ticket, %s", activateEvent)))
        .onFailure()
        .recoverWithItem(
            Response.status(Response.Status.BAD_REQUEST)
                .entity(
                    FailResponseDto.builder()
                        .message(String.format("Failed to activate ticket, %s", activateEvent))
                        .build())
                .build());
  }
}
