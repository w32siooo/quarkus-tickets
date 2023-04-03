package cygni.experiences.events;

import cygni.core.eventsourcing.BaseEvent;
import cygni.experiences.aggregates.ExperienceAggregate;
import java.util.UUID;
import lombok.Getter;

@Getter
public class ExperienceCancelledEvent extends BaseEvent {
  public static final String EXPERIENCE_CANCELLED = "ExperienceCancelled";
  public static final String AGGREGATE_TYPE = ExperienceAggregate.AGGREGATE_TYPE;

  private final String reason;

  public ExperienceCancelledEvent(UUID aggregateId, String reason) {
    super(aggregateId);
    this.reason = reason;
  }
}
