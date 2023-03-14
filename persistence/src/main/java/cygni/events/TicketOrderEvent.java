package cygni.events;

import cygni.orm.EventType;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class TicketOrderEvent implements TicketEvent {
    private String eventId;
    private UUID userId;
    private Integer quantity;
    private EventType eventType = EventType.TICKET_ORDERED;
    public TicketOrderEvent(String eventId, UUID userId, Integer quantity) {
        this.eventId = eventId;
        this.userId = userId;
        this.quantity = quantity;
    }
}
