package com.example.ledgercore.card.adapter.inbound.rest.dto;

import com.example.ledgercore.common.currency.Currency;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record AuthorizeCardPaymentRequest(

        @NotBlank
        String reference,

        @NotBlank
        String pan,

        @NotNull
        Short expiryMonth,

        @NotNull
        Short expiryYear,

        @NotBlank
        String cvv,

        String merchantReference,

        @NotNull
        @DecimalMin(value = "0.01")
        BigDecimal amount,

        @NotNull
        Currency currency
) {
}