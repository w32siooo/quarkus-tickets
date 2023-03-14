package cygni.orm;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
public class EventData {
    private UUID userId;
    private String eventId;
    private Integer quantity;
    private EventType eventType;
}
