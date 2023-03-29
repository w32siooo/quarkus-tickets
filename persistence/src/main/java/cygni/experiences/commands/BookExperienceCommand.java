package cygni.experiences.commands;

import java.util.UUID;

public record BookExperienceCommand (UUID userID, int seats) {
}
