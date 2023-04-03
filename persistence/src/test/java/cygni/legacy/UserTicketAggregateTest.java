package cygni.legacy.aggegates;

import cygni.legacy.EventData;
import cygni.legacy.EventType;
import cygni.legacy.aggregates.UserTicketAggregate;
import io.quarkus.test.junit.QuarkusTest;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@QuarkusTest
@Slf4j
public class UserTicketAggregateTest {

  @Test
  public void testApplyCreate() {
    UserTicketAggregate userTicketAggregate =
        new UserTicketAggregate(UUID.randomUUID(), UUID.randomUUID());
    EventData eventData =
        new EventData(UUID.randomUUID(), UUID.randomUUID(), 5, EventType.TICKET_CREATED);
    userTicketAggregate.applyAndValidateEvent(eventData);
  }

  @Test
  public void testApplyOrder() {

    UserTicketAggregate userTicketAggregate =
        new UserTicketAggregate(UUID.randomUUID(), UUID.randomUUID());
    EventData eventData1 =
        new EventData(UUID.randomUUID(), UUID.randomUUID(), 5, EventType.TICKET_CREATED);
    userTicketAggregate.applyAndValidateEvent(eventData1);
    EventData eventData2 =
        new EventData(UUID.randomUUID(), UUID.randomUUID(), 5, EventType.TICKET_ORDERED);
    userTicketAggregate.applyAndValidateEvent(eventData2);
  }

  @Test
  public void testApplyActivation() {
    UserTicketAggregate userTicketAggregate =
        new UserTicketAggregate(UUID.randomUUID(), UUID.randomUUID());
    EventData eventData1 =
        new EventData(UUID.randomUUID(), UUID.randomUUID(), 5, EventType.TICKET_CREATED);
    userTicketAggregate.applyAndValidateEvent(eventData1);
    EventData eventData2 =
        new EventData(UUID.randomUUID(), UUID.randomUUID(), 5, EventType.TICKET_ORDERED);
    userTicketAggregate.applyAndValidateEvent(eventData2);
    EventData eventData3 =
        new EventData(UUID.randomUUID(), UUID.randomUUID(), 5, EventType.TICKET_ACTIVATED);
    userTicketAggregate.applyAndValidateEvent(eventData3);
  }
}
