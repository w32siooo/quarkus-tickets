package cygni.users.aggregates;

import cygni.core.eventsourcing.BaseEvent;
import java.util.UUID;
import lombok.Getter;

@Getter
public class BuyExperienceEvent extends BaseEvent {
  public static final String BUY_EXPERIENCE_EVENT = "BuyExperienceEvent";
  private final UUID experienceId;

  private final Long price;

  private final int seats;

  public BuyExperienceEvent(UUID aggregateId, UUID experienceId, int seats, Long price) {
    super(aggregateId);
    this.experienceId = experienceId;
    this.seats = seats;
    this.price = price;
  }
}
