package com.example.ledgercore.withdrawal.adapter.inbound.rest.dto;

import com.example.ledgercore.common.currency.Currency;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateWithdrawalRequest(
        @NotNull
        UUID accountId,

        @NotNull
        @DecimalMin(value = "0.0001")
        BigDecimal amount,

        @NotNull
        Currency currency
) {
}