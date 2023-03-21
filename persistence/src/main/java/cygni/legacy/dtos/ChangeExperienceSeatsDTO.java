package cygni.legacy.dtos;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
public class ChangeExperienceSeatsDTO {
    @NotNull
    @Min(value = 0, message = "Number of new seats must be positive")
    private Integer newSeats;
}
