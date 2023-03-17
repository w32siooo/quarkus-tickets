package cygni.legacy.events;

import cygni.legacy.EventType;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.UUID;

@Getter
@Setter
public class TicketOrderEvent implements TicketEvent {
    @NotNull
    private UUID eventId;
    @NotNull
    private UUID userId;
    @NotNull
    @Min(1)
    private Integer quantity;
    private EventType eventType = EventType.TICKET_ORDERED;
    public TicketOrderEvent(UUID eventId, UUID userId, Integer quantity) {
        this.eventId = eventId;
        this.userId = userId;
        this.quantity = quantity;
    }
}
