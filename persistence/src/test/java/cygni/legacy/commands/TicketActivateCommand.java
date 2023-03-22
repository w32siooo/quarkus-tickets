package cygni.legacy.commands;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketActivateCommand {

    private Integer quantity;
    private UUID eventId;
    private UUID userId;
}