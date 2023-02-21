package org.acme.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.smallrye.reactive.messaging.annotations.Blocking;
import io.vertx.core.json.JsonObject;
import lombok.extern.slf4j.Slf4j;

import org.acme.hibernate.orm.panache.EventData;
import org.acme.hibernate.orm.panache.EventType;
import org.acme.hibernate.orm.panache.TicketEventDb;
import org.acme.model.TicketActivatedEvent;
import org.acme.model.TicketCreateEvent;
import org.eclipse.microprofile.reactive.messaging.Incoming;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;

@ApplicationScoped
@Slf4j
public class MessageProcessingBean {

  @Inject
  ObjectMapper objectMapper;

  @Inject
  TicketService ticketService;

  @Incoming("ticket-orders")
  @Transactional
  @Blocking
  public void consumeOrders(JsonObject command) throws JsonProcessingException {

    var event = command.mapTo(TicketCreateEvent.class);
    EventData eventData = new EventData(event.getQuantity(), event.getUserId());
    String eventId = event.getEventId();

    TicketEventDb.builder().eventId(eventId).eventType(EventType.TICKET_CREATED)
            .data(objectMapper.writeValueAsString(eventData))
            .build()
            .persist();

    log.info("order event is processed : " + event);
  }

  @Incoming("ticket-activations")
  @Transactional
  @Blocking
  public void consumeActivations(JsonObject command) throws JsonProcessingException {

    var activatedEvent = command.mapTo(TicketActivatedEvent.class);
    EventData eventData = new EventData(activatedEvent.getQuantity(), activatedEvent.getUserId());
    String eventId = activatedEvent.getEventId();

    ticketService.activateTicket(eventData,eventId);

    log.info("activate event is processed : " + activatedEvent);
  }
}
