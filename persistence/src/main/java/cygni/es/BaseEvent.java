package cygni.es;


import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Objects;
import java.util.UUID;

@Data
//@AllArgsConstructor
@NoArgsConstructor
public abstract class BaseEvent {
    protected String aggregateId;

    public BaseEvent(String aggregateId) {

        this.aggregateId = aggregateId;
    }
}
