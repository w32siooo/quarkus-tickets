package cygni.legacy.events;

import cygni.legacy.EventType;

public interface TicketEvent {
     EventType getEventType();
     Integer getQuantity();

}
