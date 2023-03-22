package cygni.experiences.services;

import cygni.experiences.commands.CancelExperienceCommand;
import cygni.experiences.commands.ChangeExperienceSeatsCommand;
import cygni.experiences.commands.CreateExperienceCommand;
import cygni.experiences.dtos.ExperienceCreatedDTO;
import io.smallrye.mutiny.Uni;

import java.util.UUID;


public interface ExperienceCommandService {

     Uni<ExperienceCreatedDTO> handle(final CreateExperienceCommand cmd);

     Uni<Void> handle (UUID aggregateID, final ChangeExperienceSeatsCommand cmd);

     Uni<Void> handle (UUID aggregateID, final CancelExperienceCommand cmd);
}
