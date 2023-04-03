package cygni.experiences.commands;

import java.util.UUID;

public record CancelExperienceCommand(UUID aggregateID, String reason) {}
