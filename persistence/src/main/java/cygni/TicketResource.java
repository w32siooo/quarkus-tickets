package cygni;

import cygni.model.TicketActivatedEvent;
import cygni.model.TicketCreateEvent;
import cygni.model.TicketOrderEvent;
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
  @Inject TicketService service;

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public Uni<Response> findByUserIdAndEventId(
      @QueryParam("eventId") @NotNull String eventId, @QueryParam("userId") @NotNull UUID userId) {
    return service.getTicketsForUser(eventId, userId).map(agg -> Response.ok(agg).build());
  }

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  @Path("create")
  public Uni<Response> create(@NotNull TicketCreateEvent create) {
    return service
        .createTicket(create)
        .map(response -> Response.status(Response.Status.CREATED).entity(response).build());
  }

  @POST
  @Consumes
  @Produces
  @Path("activate")
  public Uni<Response> activate(@NotNull TicketActivatedEvent activateEvent) {
    return service
        .activateTicket(activateEvent)
        .map(res -> Response.status(Response.Status.CREATED).entity(res).build());
  }
  @POST
    @Consumes
    @Produces
    @Path("order")
public Uni<Response> order(@NotNull TicketOrderEvent orderEvent) {
    return service
            .orderTicket(orderEvent)
            .map(res -> Response.status(Response.Status.CREATED).entity(res).build());
  }

}
