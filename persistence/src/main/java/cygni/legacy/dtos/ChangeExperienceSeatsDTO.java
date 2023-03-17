package cygni.legacy.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@AllArgsConstructor
@Getter
@Setter
public class ChangeExperienceSeatsDTO {
    @NotNull
    @Min(value = 0, message = "Number of new seats must be positive")
    private int newSeats;
}
