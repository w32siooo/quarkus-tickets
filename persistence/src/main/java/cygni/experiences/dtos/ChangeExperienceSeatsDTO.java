package cygni.experiences.dtos;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

public record ChangeExperienceSeatsDTO(@NotNull @Min(value = 0, message = "Number of new seats must be positive") Integer newSeats){

}
