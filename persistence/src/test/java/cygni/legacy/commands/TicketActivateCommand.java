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
public class TicketActivateCommand {

    private Integer quantity;
    private UUID eventId;
    private UUID userId;
}
