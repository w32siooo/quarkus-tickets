package cygni.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class TicketOrderEvent {
    private String eventId;
    private String userId;
    private Integer quantity;
}
