package cygni.users.dtos;

import java.util.List;
import java.util.UUID;

public record UserViewDTO(String name, Long balance, UUID id, List<UUID> ownedExperiences) {}
