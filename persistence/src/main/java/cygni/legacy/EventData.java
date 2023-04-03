package cygni.legacy;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class EventData {
    private UUID userId;
    private UUID eventId;
    private Integer quantity;
    private EventType eventType;
}
