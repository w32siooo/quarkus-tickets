package cygni.users.events;

import cygni.es.BaseEvent;
import java.util.UUID;

public class UserExperienceRemovedEvent extends BaseEvent {
  public static final String USER_EXPERIENCE_REMOVED = "UserExperienceRemoved";
  private final UUID experienceId;

  public UserExperienceRemovedEvent(UUID aggregateId, UUID experienceId) {
    super(aggregateId);
    this.experienceId = experienceId;
  }

  public UUID getExperienceId() {
    return experienceId;
  }
}
