package cygni.users.aggregates;

import cygni.es.BaseEvent;
import java.util.UUID;

public class UserBalanceAddedEvent extends BaseEvent {
  public static final String USER_BALANCE_CHANGED = "UserBalanceAdded";
  private final long toAdd;

  public UserBalanceAddedEvent(UUID aggregateId, long toAdd) {
    super(aggregateId);
    if (toAdd < 0)
      throw new IllegalArgumentException("toAdd must be positive" + " but was " + toAdd);
    this.toAdd = toAdd;
  }

  public long getToAdd() {
    return toAdd;
  }
}
