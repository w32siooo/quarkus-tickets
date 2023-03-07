package cygni.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import cygni.model.TicketActivatedEvent;
import cygni.model.TicketAggregate;
import cygni.model.TicketCreateEvent;
import cygni.model.TicketOrderEvent;
import cygni.panache.EventData;
import cygni.panache.EventType;
import cygni.panache.TicketEventDb;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.unchecked.Unchecked;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.reactive.mutiny.Mutiny;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.time.ZonedDateTime;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

@ApplicationScoped
@Slf4j
public class TicketService {
  @Inject Mutiny.SessionFactory sf;

  @Inject ObjectMapper objectMapper;


  //order ticket if there are available tickets
  public Uni<Void> orderTicket(TicketOrderEvent ev){
      return this.sf.withTransaction(
              (session, transaction) ->
                      session
                              .createNamedQuery("Tickets.findAllByEventId", TicketEventDb.class)
                              .setParameter("eventId", ev.getEventId())
                              .getResultList()
                              .map(
                                      resultList -> {
                                          int availableTickets = 0;
                                          for (TicketEventDb eventDb : resultList) {
                                              EventData eventData;
                                              try {
                                                  eventData =
                                                          objectMapper.readValue(eventDb.getData(), EventData.class);
                                              } catch (JsonProcessingException e) {
                                                  throw new RuntimeException("Jackson kunne ikke serialisere " + e);
                                              }
                                              if (eventDb.getEventType().equals(EventType.TICKET_CREATED)) {
                                                  availableTickets += eventData.getQuantity();
                                              }
                                          }
                                          if (availableTickets >= ev.getQuantity()) {
                                              TicketEventDb ticketEventDb = new TicketEventDb();
                                              ticketEventDb.setEventId(ev.getEventId());
                                              ticketEventDb.setEventType(EventType.TICKET_ORDERED);
                                              ticketEventDb.setUserId(UUID.fromString(ev.getUserId()));
                                              EventData eventData = new EventData(ev.getQuantity(), UUID.fromString(ev.getUserId()));
                                              try {
                                                  ticketEventDb.setData(objectMapper.writeValueAsString(eventData));
                                              } catch (JsonProcessingException e) {
                                                  throw new RuntimeException("Jackson kunne ikke serialisere " + e);
                                              }
                                              log.info("Ticket ordered for event: " + ev.getEventId());
                                              log.info(ticketEventDb.toString());
                                             return ticketEventDb;
                                          }else {
                                                log.info("Not enough tickets available for event: " + ev.getEventId());
                                              return null;
                                          }
                                      }
                                      )
      ).map(
              Unchecked.function(ticketEventDb -> {
                  if (ticketEventDb != null) {
                      return this.sf.withTransaction(
                              (session, transaction) -> session.persist(ticketEventDb)
                      );
                  } else {
                      throw new RuntimeException("Not enough tickets available");
                  }
              })
      ).flatMap(
              uni -> uni
      );
  }

  public Uni<TicketEventDb> createTicket(TicketCreateEvent ev) {
    TicketEventDb ticketEventDb = new TicketEventDb();
    ticketEventDb.setEventId(ev.getEventId());
    ticketEventDb.setEventType(EventType.TICKET_CREATED);
    ticketEventDb.setUserId(ev.getUserId());
    EventData eventData = new EventData(ev.getQuantity(), ev.getUserId());
    try {
      ticketEventDb.setData(objectMapper.writeValueAsString(eventData));
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
    return sf.withTransaction(session -> session.persist(ticketEventDb))
        .replaceWith(Uni.createFrom().item(ticketEventDb));
  }

  public Uni<TicketAggregate> getTicketsForUser(String eventId, UUID userId) {
    AtomicInteger activatedTickets = new AtomicInteger(0);
    AtomicInteger inactiveTickets = new AtomicInteger(0);

    return this.sf.withTransaction(
        (session, transaction) ->
            session
                .createNamedQuery("Tickets.findAllByEventIdAndUserId", TicketEventDb.class)
                .setParameter("eventId", eventId)
                .setParameter("userId", userId)
                .getResultList()
                .map(
                    resultList -> {
                      resultList.forEach(
                          eventDb -> {
                            EventData eventData;
                            try {
                              eventData =
                                  objectMapper.readValue(eventDb.getData(), EventData.class);
                            } catch (JsonProcessingException e) {
                              throw new RuntimeException("Jackson kunne ikke serialisere " + e);
                            }
                            if (eventDb.getEventType().equals(EventType.TICKET_ORDERED)) {
                              inactiveTickets.addAndGet(eventData.getQuantity());

                            } else if (eventDb.getEventType().equals(EventType.TICKET_ACTIVATED)) {
                              activatedTickets.addAndGet(eventData.getQuantity());
                            }
                          });

                      return new TicketAggregate(
                          userId,
                          activatedTickets.get(),
                          inactiveTickets.get(),
                          ZonedDateTime.now());
                    }));
  }

  public Uni<TicketEventDb> activateTicket(TicketActivatedEvent ev) {
    TicketEventDb ticketEventDb = new TicketEventDb();
    ticketEventDb.setEventId(ev.getEventId());
    ticketEventDb.setEventType(EventType.TICKET_ACTIVATED);
    ticketEventDb.setUserId(ev.getUserId());
    EventData eventData = new EventData(ev.getQuantity(), ev.getUserId());
    try {
      ticketEventDb.setData(objectMapper.writeValueAsString(eventData));
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
    return this.sf
        .withTransaction((s, t) -> s.persist(ticketEventDb))
        .replaceWith(Uni.createFrom().item(ticketEventDb));
  }
  ;
}
