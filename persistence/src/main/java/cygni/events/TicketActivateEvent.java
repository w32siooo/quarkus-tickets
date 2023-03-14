package cygni.events;

import cygni.orm.EventType;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class TicketActivateEvent implements TicketEvent   {
    private String eventId;
    private UUID userId;
    private Integer quantity;
    private EventType eventType = EventType.TICKET_ACTIVATED;

    public TicketActivateEvent(String eventId, UUID userId, Integer quantity) {
        this.eventId = eventId;
        this.userId = userId;
        this.quantity = quantity;
    }
}

