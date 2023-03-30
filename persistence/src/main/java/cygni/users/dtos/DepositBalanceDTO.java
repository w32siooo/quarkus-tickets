package cygni.users.dtos;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

public record DepositBalanceDTO(@NotNull @Min(0) Long amount) {
}
