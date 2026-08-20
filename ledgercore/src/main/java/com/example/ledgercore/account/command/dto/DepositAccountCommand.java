package com.example.ledgercore.account.command.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record DepositAccountCommand(

        @NotNull
        UUID accountId,

        @NotNull
        @DecimalMin("0.0001")
        BigDecimal amount

) {
}