package cygni.legacy;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class EventData {
    private UUID userId;
    private UUID eventId;
    private Integer quantity;
    private EventType eventType;
}
