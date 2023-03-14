package cygni.aggegates;

import cygni.aggregates.TicketAggregate;
import cygni.events.TicketCreateEvent;
import cygni.events.TicketEvent;
import cygni.orm.EventData;
import cygni.orm.EventType;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.mutiny.unchecked.Unchecked;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.UUID;

@QuarkusTest
@Slf4j
public class TicketAggregateTest {

    @Test
    public void testApplyCreate() {
        TicketAggregate ticketAggregate = new TicketAggregate("test");
        EventData eventData = new EventData(UUID.randomUUID(),"test",5, EventType.TICKET_CREATED);
        ticketAggregate.applyEvent(eventData).subscribe().with(
                ticket -> log.info("Ticket created successfully"),
                failure -> log.error("Ticket creation failed: {}", failure.getMessage())
        );
    }
    @Test
    public void testApplyOrder() {

        TicketAggregate ticketAggregate = new TicketAggregate("test");
        EventData eventData1 = new EventData(UUID.randomUUID(),"test",5, EventType.TICKET_CREATED);
        ticketAggregate.applyEvent(eventData1).subscribe().with(
                ticket -> log.info("Ticket created successfully"),
                failure -> log.error("Ticket creation failed: {}", failure.getMessage())
        );
        EventData eventData2 = new EventData(UUID.randomUUID(),"test",5, EventType.TICKET_ORDERED);
        ticketAggregate.applyEvent(eventData2).subscribe().with(
                ticket -> log.info("Ticket ordered successfully"),
                Unchecked.consumer(failure -> {log.error("Ticket order failed: {}", failure.getMessage());
                    throw new RuntimeException(failure);
                })
        );
    }
    @Test
    public void testApplyActivation(){
        TicketAggregate ticketAggregate = new TicketAggregate("test");
        EventData eventData1 = new EventData(UUID.randomUUID(),"test",5, EventType.TICKET_CREATED);
        ticketAggregate.applyEvent(eventData1).subscribe().with(
                ticket -> log.info("Ticket created successfully"),
                failure -> log.error("Ticket creation failed: {}", failure.getMessage())
        );
        EventData eventData2 = new EventData(UUID.randomUUID(),"test",5, EventType.TICKET_ORDERED);
        ticketAggregate.applyEvent(eventData2).subscribe().with(
                ticket -> log.info("Ticket ordered successfully"),
                Unchecked.consumer(failure -> {log.error("Ticket order failed: {}", failure.getMessage());
                    throw new RuntimeException(failure);
                })
        );
        EventData eventData3 = new EventData(UUID.randomUUID(),"test",5, EventType.TICKET_ACTIVATED);
        ticketAggregate.applyEvent(eventData3).subscribe().with(
                ticket -> log.info("Ticket activated successfully"),
                Unchecked.consumer(failure -> {log.error("Ticket activation failed: {}", failure.getMessage());
                    throw new RuntimeException(failure);
                })
        );
    }
}
