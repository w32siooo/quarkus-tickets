package cygni.experiences.events;

import cygni.es.BaseEvent;
import cygni.experiences.aggregates.ExperienceAggregate;
import java.util.UUID;
import lombok.Getter;

@Getter
public class ExperienceTotalSeatsChangedEvent extends BaseEvent {

  public static final String EXPERIENCE_TOTAL_SEATS_CHANGED = "ExperienceTotalSeatsChanged";

  public static final String AGGREGATE_TYPE = ExperienceAggregate.AGGREGATE_TYPE;

  private final int newSeats;

  public ExperienceTotalSeatsChangedEvent(UUID aggregateId, int newSeats) {
    super(aggregateId);
    this.newSeats = newSeats;
  }
}
