package cygni.users.dtos;

import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public record CreateNewUserDTO(@NotNull @NotBlank @Length(min = 3,max = 20) String name, @Min(0) Long balance) {
}
