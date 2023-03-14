package cygni.events;

import cygni.orm.EventType;
import lombok.Builder;

public interface TicketEvent {
     EventType getEventType();
     Integer getQuantity();

}
