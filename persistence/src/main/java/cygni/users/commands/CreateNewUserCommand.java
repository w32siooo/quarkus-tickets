package cygni.users.commands;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Length;

import java.util.UUID;

public record CreateNewUserCommand(
    @NotNull @NotBlank @Length(min = 3, max = 20) String name, @Min(0) Long balance, UUID id) {}
