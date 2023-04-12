package cygni.users.aggregates;

import cygni.es.BaseEvent;
import java.util.UUID;

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

  public UUID getExperienceId() {
    return experienceId;
  }

  public Long getPrice() {
    return price;
  }

  public int getSeats() {
    return seats;
  }
}
