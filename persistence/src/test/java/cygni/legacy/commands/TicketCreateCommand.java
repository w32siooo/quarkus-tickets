package cygni.legacy.commands;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class TicketCreateCommand {
  private UUID userId;
  private UUID eventId;
  private Integer quantity;
}
