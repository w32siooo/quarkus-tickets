package cygni.es;

import java.util.UUID;

public abstract class BaseEvent {
  protected UUID aggregateId;

  public BaseEvent(UUID aggregateId) {

    this.aggregateId = aggregateId;
  }
}
