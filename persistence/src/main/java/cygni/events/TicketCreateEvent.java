package cygni.events;

import cygni.orm.EventType;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter

public class TicketCreateEvent implements TicketEvent{
    private UUID userId;
    private String eventId;
    private Integer quantity;
    private EventType eventType = EventType.TICKET_CREATED;
    public TicketCreateEvent(UUID userId, String eventId, Integer quantity) {
        this.userId = userId;
        this.eventId = eventId;
        this.quantity = quantity;
    }
}
