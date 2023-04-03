package cygni.experiences.events;

import cygni.core.eventsourcing.BaseEvent;
import java.util.UUID;

public class ExperienceBookedEvent extends BaseEvent {
  public static final String EXPERIENCE_BOOKED = "ExperienceBooked";
  public static final String AGGREGATE_TYPE = "Experience";

  private final UUID userId;
  private final int seats;

  public ExperienceBookedEvent(UUID aggregateId, UUID userId, int seats) {
    super(aggregateId);
    this.userId = userId;
    this.seats = seats;
  }

  public UUID getUserId() {
    return userId;
  }

  public int getSeats() {
    return seats;
  }
}
