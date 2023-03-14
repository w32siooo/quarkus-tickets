package cygni.aggregates;


import com.fasterxml.jackson.databind.ObjectMapper;
import cygni.orm.EventData;
import cygni.orm.TicketEventDb;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.unchecked.Unchecked;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Getter
@Setter
@Slf4j
@AllArgsConstructor
public class TicketAggregate {
    private String eventId;

    private AtomicInteger version;

    private AtomicInteger activatedTickets;
    private AtomicInteger inactiveTickets;

    private AtomicInteger unBookedTickets;

    private ZonedDateTime time;


    public TicketAggregate(String eventId) {
        this.eventId = eventId;
        this.activatedTickets = new AtomicInteger(0);
        this.inactiveTickets = new AtomicInteger(0);
        this.unBookedTickets = new AtomicInteger(0);
        this.time = ZonedDateTime.now();
        this.version = new AtomicInteger(0);
    }


    public void applyEvent(EventData event) {
        switch (event.getEventType()) {
            case TICKET_CREATED -> unBookedTickets.addAndGet(event.getQuantity());
            case TICKET_ORDERED -> {
                if (unBookedTickets.get() < event.getQuantity()) {
                    throw new RuntimeException("Not enough tickets to order");
                }
                inactiveTickets.addAndGet(event.getQuantity());
                unBookedTickets.addAndGet(-event.getQuantity());
            }
            case TICKET_ACTIVATED -> {
                if (inactiveTickets.get() < event.getQuantity()) {
                    throw new RuntimeException("Not enough tickets to activate");
                }
                activatedTickets.addAndGet(event.getQuantity());
                inactiveTickets.addAndGet(-event.getQuantity());
            }
        }
        log.info("unbooked tickets are now " + unBookedTickets.get());

        this.version.incrementAndGet();
    }

    TicketAggregate getCopy(){
        return new TicketAggregate(this.eventId,this.version,this.activatedTickets,
                this.inactiveTickets,this.unBookedTickets,ZonedDateTime.now());
    }


}
