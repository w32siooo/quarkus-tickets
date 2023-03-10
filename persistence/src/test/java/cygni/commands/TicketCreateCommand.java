package cygni.commands;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class TicketCreateCommand {
    private UUID userId;
    private String eventId;
    private Integer quantity;
}
