package cygni.core.eventsourcing;

import java.util.UUID;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public abstract class BaseEvent {
  protected UUID aggregateId;

  public BaseEvent(UUID aggregateId) {

    this.aggregateId = aggregateId;
  }
}
