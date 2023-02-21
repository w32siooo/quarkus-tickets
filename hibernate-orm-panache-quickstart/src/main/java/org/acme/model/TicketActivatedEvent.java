package org.acme.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Builder
@Getter
@Setter
public class TicketActivatedEvent   {
    private String eventId;
    private UUID userId;
    private Integer quantity;
}

