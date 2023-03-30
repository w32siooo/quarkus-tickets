package cygni.users.components;

import cygni.users.dtos.UserViewDTO;
import io.smallrye.mutiny.Uni;

import java.util.UUID;

public interface UserQueryService {

    Uni<UserViewDTO> handle(UUID userId);


}
