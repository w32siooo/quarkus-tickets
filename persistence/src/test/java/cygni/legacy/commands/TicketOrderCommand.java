package cygni.legacy.commands;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketOrderCommand {
    private UUID eventId;
    private Integer quantity;
    private UUID userId;
}
