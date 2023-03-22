package cygni.experiences.services;

import cygni.experiences.commands.CancelExperienceCommand;
import cygni.experiences.commands.ChangeExperienceSeatsCommand;
import cygni.experiences.commands.CreateExperienceCommand;
import cygni.experiences.dtos.ExperienceCreatedDTO;
import io.smallrye.mutiny.Uni;




public interface ExperienceCommandService {

     Uni<ExperienceCreatedDTO> handle(final CreateExperienceCommand cmd);

     Uni<Void> handle (String aggregateID, final ChangeExperienceSeatsCommand cmd);

     Uni<Void> handle (String aggregateID, final CancelExperienceCommand cmd);
}
