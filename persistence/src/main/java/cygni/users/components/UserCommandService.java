package cygni.users.components;

import cygni.es.dto.RequestAcceptedDTO;
import cygni.users.dtos.BuyTicketDTO;
import cygni.users.commands.CreateNewUserCommand;
import cygni.users.dtos.RemoveTicketDTO;
import io.smallrye.mutiny.Uni;
import java.util.UUID;

public interface UserCommandService {

  Uni<RequestAcceptedDTO> handle(CreateNewUserCommand createUserDTO);

    Uni<RequestAcceptedDTO> handle(UUID aggregateId ,BuyTicketDTO buyTicketDTO);
    Uni<RequestAcceptedDTO> handle(UUID aggregateId, RemoveTicketDTO removeTicketDTO);
    Uni<RequestAcceptedDTO> handle(UUID aggregateId, Long newBalance);
}
