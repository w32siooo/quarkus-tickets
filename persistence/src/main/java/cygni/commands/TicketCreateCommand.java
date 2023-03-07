package cygni.commands;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor

public class TicketCreateCommand {

    private String eventId;
    private Integer quantity;
    private UUID userId;



}
