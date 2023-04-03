package cygni.users.aggregates;

import cygni.core.eventsourcing.BaseEvent;
import java.util.UUID;
import lombok.Getter;

@Getter
public class UserCreatedEvent extends BaseEvent {
  public static final String USER_CREATED = "UserCreated";
  private final String name;
  private final long balance;

  public UserCreatedEvent(UUID aggregateId, String name, long balance) {
    super(aggregateId);
    this.name = name;
    this.balance = balance;
  }

  public String getName() {
    return name;
  }

  public long getBalance() {
    return balance;
  }
}
