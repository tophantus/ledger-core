package com.example.ledgercore.transaction.command.dto;

import com.example.ledgercore.common.currency.Currency;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateTransferIntentCommand(

        @NotNull
        UUID sourceAccountId,

        @NotBlank
        @Size(max = 30)
        String destinationAccountNo,

        @NotNull
        @DecimalMin(value = "0.0001")
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