package cygni.tickets.services;

import cygni.tickets.commands.ChangeExperienceSeatsCommand;
import cygni.tickets.commands.CreateExperienceCommand;
import cygni.tickets.dtos.ExperienceCreatedDTO;
import io.smallrye.mutiny.Uni;




public interface ExperienceCommandService {

     Uni<ExperienceCreatedDTO> handle(final CreateExperienceCommand cmd);

     Uni<Void> handle (String aggregateID, final ChangeExperienceSeatsCommand cmd);
}
