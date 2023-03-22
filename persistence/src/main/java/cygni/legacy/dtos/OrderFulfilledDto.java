package cygni.legacy.dtos;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@Builder
@NoArgsConstructor

public class OrderFulfilledDto implements ResponseDto {
    private UUID eventId;
    private Integer quantity;
}