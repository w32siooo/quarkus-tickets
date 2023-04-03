package cygni.users.dtos;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Length;

public record CreateNewUserDTO(
    @NotNull @NotBlank @Length(min = 3, max = 20) String name, @Min(0) Long balance) {}
