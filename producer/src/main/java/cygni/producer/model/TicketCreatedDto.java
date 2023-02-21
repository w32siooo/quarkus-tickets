package cygni.producer.model;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Builder
@RegisterForReflection
@Data
public class TicketCreatedDto {
    private UUID userId;
    private String eventId;
    private Integer quantity;
}
