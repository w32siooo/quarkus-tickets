package cygni.users.components;

import cygni.users.dtos.BuyTicketDTO;
import cygni.users.dtos.CreateNewUserDTO;
import cygni.users.dtos.RemoveTicketDTO;
import io.smallrye.mutiny.Uni;

import java.util.UUID;

public interface UserCommandService {

    Uni<Void> handle(CreateNewUserDTO createUserDTO);
    Uni<Void> handle(UUID aggregateId ,BuyTicketDTO buyTicketDTO);
    Uni<Void> handle(UUID aggregateId, RemoveTicketDTO removeTicketDTO);
    Uni<Void> handle(UUID aggregateId, Long newBalance);
}
