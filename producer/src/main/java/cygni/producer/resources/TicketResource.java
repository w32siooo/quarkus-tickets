package cygni.producer.resources;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

import cygni.producer.commands.TicketActivateCommand;
import cygni.producer.commands.TicketOrderCommand;
import cygni.producer.model.TicketActivatedDto;
import cygni.producer.model.TicketCreatedDto;
import cygni.producer.services.TicketProducerService;
import io.smallrye.mutiny.Uni;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/api/tickets")
public class TicketResource {

  private final Logger logger = LoggerFactory.getLogger(TicketResource.class);
  @Inject TicketProducerService ticketProducerService;

  /**
   * Endpoint to generate a new quote request id and send it to "quote-requests" channel (which maps
   * to the "quote-requests" RabbitMQ exchange) using the emitter.
   */
  @POST
  @Path("/create")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  public Uni<TicketCreatedDto> orderTickets(TicketOrderCommand ticketOrderCommand) {

    return ticketProducerService
        .orderTicket(ticketOrderCommand)
        .replaceWith(
            Uni.createFrom()
                .item(
                    TicketCreatedDto.builder()
                        .eventId(ticketOrderCommand.getEventId())
                        .quantity(ticketOrderCommand.getQuantity())
                        .userId(ticketOrderCommand.getUserId())
                        .build()));
  }

  @POST
  @Path("/activate")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  public Uni<TicketActivatedDto> activateTickets(TicketActivateCommand ticketActivateCommand) {

    return ticketProducerService
        .activateTicket(ticketActivateCommand)
        .replaceWith(
            Uni.createFrom()
                .item(
                    TicketActivatedDto.builder()
                        .eventId(ticketActivateCommand.getEventId())
                        .quantity(ticketActivateCommand.getQuantity())
                        .userId(ticketActivateCommand.getUserId())
                        .build()));
  }
}
