package cygni.producer.services;

import cygni.producer.commands.TicketActivateCommand;
import cygni.producer.commands.TicketCreateCommand;
import cygni.producer.commands.TicketOrderCommand;
import io.smallrye.mutiny.Uni;
import io.smallrye.reactive.messaging.MutinyEmitter;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.reactive.messaging.*;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
@Slf4j
public class TicketProducerService {
  @OnOverflow(value = OnOverflow.Strategy.BUFFER, bufferSize = 10)
  @Inject

  @Channel("ticket-create-request")
  MutinyEmitter<TicketCreateCommand> createCommandMutinyEmitter;

  @Channel("ticket-activate-request")
  MutinyEmitter<TicketActivateCommand> activateCommandMutinyEmitter;

  @Channel("ticket-order-request")
  MutinyEmitter<TicketOrderCommand> orderCommandMutinyEmitter;


  public Uni<Void> createTicket(TicketCreateCommand ticketCreateCommand) {


    return createCommandMutinyEmitter.send(ticketCreateCommand);

  }

  public  Uni<Void> activateTicket(TicketActivateCommand ticketActivateCommand) {

    return activateCommandMutinyEmitter.send(ticketActivateCommand);
  }

  public Uni<Void> orderTickets(TicketOrderCommand ticketOrderCommand) {
    return orderCommandMutinyEmitter.send(ticketOrderCommand);
  }
}
