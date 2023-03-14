package cygni.aggegates;

import cygni.aggregates.TicketAggregate;
import cygni.orm.EventData;
import cygni.orm.EventType;
import io.quarkus.test.junit.QuarkusTest;
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
        ticketAggregate.applyAndValidateEvent(eventData);
    }
    @Test
    public void testApplyOrder() {

        TicketAggregate ticketAggregate = new TicketAggregate("test");
        EventData eventData1 = new EventData(UUID.randomUUID(),"test",5, EventType.TICKET_CREATED);
        ticketAggregate.applyAndValidateEvent(eventData1);
        EventData eventData2 = new EventData(UUID.randomUUID(),"test",5, EventType.TICKET_ORDERED);
        ticketAggregate.applyAndValidateEvent(eventData2);
    }
   @Test
    public void testApplyActivation(){
        TicketAggregate ticketAggregate = new TicketAggregate("test");
        EventData eventData1 = new EventData(UUID.randomUUID(),"test",5, EventType.TICKET_CREATED);
        ticketAggregate.applyAndValidateEvent(eventData1);
        EventData eventData2 = new EventData(UUID.randomUUID(),"test",5, EventType.TICKET_ORDERED);
        ticketAggregate.applyAndValidateEvent(eventData2);
        EventData eventData3 = new EventData(UUID.randomUUID(),"test",5, EventType.TICKET_ACTIVATED);
        ticketAggregate.applyAndValidateEvent(eventData3);
    }
}
