package cygni.users.aggregates;

import cygni.core.eventsourcing.BaseEvent;
import java.util.UUID;
import lombok.Getter;

@Getter
public class UserExperienceRemovedEvent extends BaseEvent {
  public static final String USER_EXPERIENCE_REMOVED = "UserExperienceRemoved";
  private final UUID experienceId;

  public UserExperienceRemovedEvent(UUID aggregateId, UUID experienceId) {
    super(aggregateId);
    this.experienceId = experienceId;
  }
}
