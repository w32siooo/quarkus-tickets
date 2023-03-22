package cygni.legacy.aggregates;

import cygni.es.AggregateRoot;
import cygni.es.Event;
import cygni.legacy.EventData;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

@Getter
@Setter
public class EventAggregate extends AggregateRoot {

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

    @Override
    public void when(Event event) {


    }

    @Override
    public String getAGGREGATE_TYPE() {
        return null;
    }
}
