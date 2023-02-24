package cygni.producer.model;


import lombok.Getter;
import lombok.Value;

import java.time.ZonedDateTime;
import java.util.UUID;

@Value
@Getter
public class TicketAggregate {
    UUID userId;
    Integer activatedTickets;
    Integer inactiveTickets;
    ZonedDateTime time;
}
