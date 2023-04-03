package cygni.legacy.dtos;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@Builder
@NoArgsConstructor

public class OrderFulfilledDto implements ResponseDto {
    private UUID eventId;
    private Integer quantity;
}
