package cygni.users.dtos;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record DepositBalanceDTO(@NotNull @Min(0) Long amount) {
}
