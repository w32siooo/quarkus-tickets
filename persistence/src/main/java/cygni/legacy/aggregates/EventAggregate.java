package cygni.legacy.aggregates;

import cygni.es.Event;
import cygni.legacy.EventData;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EventAggregate {

  private AtomicInteger unBookedTickets;
  private AtomicInteger bookedTickets;
  private UUID eventId;

  public EventAggregate(UUID eventId) {
    this.eventId = eventId;
    this.unBookedTickets = new AtomicInteger(0);
    this.bookedTickets = new AtomicInteger(0);
  }

  public void applyAndValidateEvent(EventData event) {
    switch (event.getEventType()) {
      case TICKET_CREATED -> {
        unBookedTickets.addAndGet(event.getQuantity());
      }
      case TICKET_ORDERED -> {
        if (unBookedTickets.get() < event.getQuantity()) {
          throw new RuntimeException("Not enough tickets to order");
        }
        bookedTickets.addAndGet(event.getQuantity());
        unBookedTickets.addAndGet(-event.getQuantity());
      }
    }
  }

  public void when(Event event) {}
}
