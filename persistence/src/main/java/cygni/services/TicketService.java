package cygni.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import cygni.model.TicketCreateEvent;
import cygni.panache.EventData;
import cygni.panache.EventType;
import cygni.panache.TicketEventDb;
import io.smallrye.mutiny.Uni;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.reactive.mutiny.Mutiny;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

@ApplicationScoped
@Slf4j
public class TicketService {
  @Inject Mutiny.SessionFactory sf;

  @Inject ObjectMapper objectMapper;

  public Uni<TicketEventDb> createTicket(TicketCreateEvent ev) {
    TicketEventDb ticketEventDb = new TicketEventDb();
    ticketEventDb.setEventId(ev.getEventId());
    ticketEventDb.setEventType(EventType.TICKET_CREATED);
    EventData eventData = new EventData(ev.getQuantity(), ev.getUserId());
    try {
      ticketEventDb.setData(objectMapper.writeValueAsString(eventData));
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
    return sf.withTransaction(session -> session.persist(ticketEventDb))
        .replaceWith(Uni.createFrom().item(ticketEventDb));
  }

  public Uni<List<TicketEventDb>> getTicketsForUser(String eventId, UUID userId) {
    AtomicInteger activatedTickets = new AtomicInteger(0);
    AtomicInteger inactiveTickets = new AtomicInteger(0);

    return this.sf
            .withTransaction((session,transaction) -> session
                    .createNamedQuery("Tickets.findAll",TicketEventDb.class)
                    .setParameter("eventId",eventId)
                    .getResultList())
            .log();
  };
}
