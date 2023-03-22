package cygni.es;


import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Objects;
import java.util.UUID;

@Data
@NoArgsConstructor
public abstract class BaseEvent {
    protected UUID aggregateId;

    public BaseEvent(UUID aggregateId) {

        this.aggregateId = aggregateId;
    }
}
