package cygni.users.components;

import cygni.users.dtos.BuyTicketDTO;
import io.smallrye.mutiny.Uni;

import java.util.UUID;

public interface UserCommandService {

    Uni<Void> handle(String name, Long balance);
    Uni<Void> handle(UUID aggregateId ,BuyTicketDTO buyTicketDTO);
    Uni<Void> removeTicket(UUID userId, UUID experienceId);
    Uni<Void> handle(UUID userId, Long newBalance);
}
