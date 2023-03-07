package cygni.producer.services;

import cygni.producer.commands.TicketActivateCommand;
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

  @Channel("ticket-orders")
  MutinyEmitter<TicketOrderCommand> createCommandMutinyEmitter;

  @Channel("ticket-activations")
  MutinyEmitter<TicketActivateCommand> activateCommandMutinyEmitter;

  public Uni<Void> orderTicket(TicketOrderCommand ticketOrderCommand) {

    log.info("sending ticket to ticket-requests");

    return createCommandMutinyEmitter.send(ticketOrderCommand);

  }

  public  Uni<Void> activateTicket(TicketActivateCommand ticketActivateCommand) {

    return activateCommandMutinyEmitter.send(ticketActivateCommand);
  }
}
