package cygni.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import cygni.model.TicketCreateEvent;
import cygni.panache.EventData;
import cygni.panache.EventType;
import cygni.panache.TicketEventDb;
import io.smallrye.mutiny.Uni;
import org.hibernate.reactive.mutiny.Mutiny;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
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
}
