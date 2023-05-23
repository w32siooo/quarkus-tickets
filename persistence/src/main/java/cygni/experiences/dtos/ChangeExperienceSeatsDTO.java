package cygni.experiences.dtos;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ChangeExperienceSeatsDTO(
        @NotNull @Min(value = 0, message = "Number of new seats must be positive") Integer newSeats) {}
