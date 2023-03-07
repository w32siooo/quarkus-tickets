package cygni.producer.model;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Builder;
import lombok.Data;

@Builder
@RegisterForReflection
@Data
public class TicketOrderedDto {
    private Integer quantity;
    private String eventId;
    private String userId;
}
