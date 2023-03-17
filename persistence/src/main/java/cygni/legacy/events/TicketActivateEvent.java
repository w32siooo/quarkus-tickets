package cygni.legacy.events;

import cygni.legacy.EventType;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.UUID;

@Getter
@Setter
public class TicketActivateEvent implements TicketEvent   {
    @NotNull
    private UUID eventId;
    @NotNull
    private UUID userId;
    @Min(1)
    @NotNull
    private Integer quantity;
    private EventType eventType = EventType.TICKET_ACTIVATED;

    public TicketActivateEvent(UUID eventId, UUID userId, Integer quantity) {
        this.eventId = eventId;
        this.userId = userId;
        this.quantity = quantity;
    }
}

