package org.acme.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.acme.hibernate.orm.panache.EventData;
import org.acme.hibernate.orm.panache.EventType;
import org.acme.hibernate.orm.panache.TicketEventDb;
import org.acme.model.TicketActivatedEvent;
import org.acme.model.TicketAggregate;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static java.util.stream.Collectors.groupingBy;

@ApplicationScoped
@Slf4j
public class TicketService {

  @Inject ObjectMapper objectMapper;

  @Transactional
  public TicketAggregate getTicketsForUser(String eventId, UUID userId) {
    AtomicInteger activatedTickets = new AtomicInteger(0);
    AtomicInteger inactiveTickets = new AtomicInteger(0);
    try (Stream<TicketEventDb> eventDbStream = TicketEventDb.stream("eventId", eventId)) {
      var groupedEvents = eventDbStream.collect(groupingBy(TicketEventDb::getEventType));

      if (groupedEvents.get(EventType.TICKET_CREATED) != null) {

        groupedEvents.get(EventType.TICKET_CREATED).stream()
            .map(
                created -> {
                  try {
                    return objectMapper.readValue(created.getData(), EventData.class);
                  } catch (JsonProcessingException e) {
                    log.error("could not deserialize");
                    return null;
                  }
                })
            .forEach(
                data -> {
                  if (data.getUserId().equals(userId)) {
                    inactiveTickets.addAndGet(data.getQuantity());
                  }
                });
      }
      if (groupedEvents.get(EventType.TICKET_ACTIVATED) != null) {
        groupedEvents.get(EventType.TICKET_ACTIVATED).stream()
            .map(
                created -> {
                  try {
                    return objectMapper.readValue(created.getData(), EventData.class);
                  } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                  }
                })
            .forEach(
                data -> {
                  if (data.getUserId().equals(userId)) {
                    inactiveTickets.addAndGet(-data.getQuantity());
                    activatedTickets.addAndGet(data.getQuantity());
                  }
                });
      }
    }catch (Exception e){
        log.error(e.getMessage());
    }
      return new TicketAggregate(
        userId, activatedTickets.get(), inactiveTickets.get(), ZonedDateTime.now());
  }

  @Transactional
    public TicketActivatedEvent activateTicket(EventData eventData, String eventId)
            throws JsonProcessingException {
        if (eventData.getQuantity() < 0) {
            throw new IllegalArgumentException();
        }
        TicketAggregate res = getTicketsForUser(eventId, eventData.getUserId());
        AtomicInteger activatedTickets = new AtomicInteger(res.getActivatedTickets());
        AtomicInteger inactiveTickets = new AtomicInteger(res.getInactiveTickets());
        if (inactiveTickets.get() < eventData.getQuantity()) {
            log.error("not enough tickets");
        } else {
            TicketEventDb eventDb =
                    TicketEventDb.builder()
                            .eventType(EventType.TICKET_ACTIVATED)
                            .data(objectMapper.writeValueAsString(eventData))
                            .eventId(eventId)
                            .build();
            eventDb.persist();
            inactiveTickets.addAndGet(-eventData.getQuantity());
            activatedTickets.addAndGet(eventData.getQuantity());
        }
        return TicketActivatedEvent.builder()
                .quantity(eventData.getQuantity())
                .userId(eventData.getUserId())
                .build();
    }
}
