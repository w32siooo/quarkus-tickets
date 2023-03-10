package cygni.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class OrderFulfilledDto implements ResponseDto {
    private String eventId;
    private Integer quantity;
}
