package cygni.legacy.aggregates;

import cygni.legacy.EventData;
import java.time.ZonedDateTime;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Getter
@Setter
@Slf4j
@AllArgsConstructor
public class UserTicketAggregate {
  private UUID eventId;
  private AtomicInteger version;
  private AtomicInteger activatedTickets;
  private AtomicInteger inactiveTickets;
  private UUID userId;
  private ZonedDateTime time;

  public UserTicketAggregate(UUID eventId, UUID userId) {
    this.eventId = eventId;
    this.userId = userId;
    this.version = new AtomicInteger(0);
    this.activatedTickets = new AtomicInteger(0);
    this.inactiveTickets = new AtomicInteger(0);
    this.time = ZonedDateTime.now();
  }

  public void applyAndValidateEvent(EventData event) {
    switch (event.getEventType()) {
      case TICKET_ORDERED -> {
        inactiveTickets.addAndGet(event.getQuantity());
      }
      case TICKET_ACTIVATED -> {
        if (inactiveTickets.get() < event.getQuantity()) {
          throw new RuntimeException("Not enough tickets to activate");
        }
        activatedTickets.addAndGet(event.getQuantity());
        inactiveTickets.addAndGet(-event.getQuantity());
      }
    }
    this.version.incrementAndGet();
  }
}
