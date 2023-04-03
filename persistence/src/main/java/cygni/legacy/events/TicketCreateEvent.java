package cygni.legacy.events;

import cygni.legacy.EventType;
import java.util.UUID;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TicketCreateEvent implements TicketEvent {
  @NotNull private UUID userId;
  @NotNull private UUID eventId;

  @Min(1)
  @NotNull
  private Integer quantity;

  private EventType eventType = EventType.TICKET_CREATED;

  public TicketCreateEvent(UUID userId, UUID eventId, Integer quantity) {
    this.userId = userId;
    this.eventId = eventId;
    this.quantity = quantity;
  }
}
