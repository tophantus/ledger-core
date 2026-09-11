package com.example.ledgercore.transaction.command.dto;

import com.example.ledgercore.common.currency.Currency;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.UUID;

public record WithdrawMoneyCommand(

        @NotNull
        UUID sourceAccountId,

        @NotNull
        @DecimalMin(
                value = "0.0001",
                message = "Withdraw amount must be greater than zero"
        )
        BigDecimal amount,

        @NotNull
        Currency currency,

        @NotBlank
        @Size(max = 50)
        String reference,

        @Size(max = 500)
        String description

) {
}