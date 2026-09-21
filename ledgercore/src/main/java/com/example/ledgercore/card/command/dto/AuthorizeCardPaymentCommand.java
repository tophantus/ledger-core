package com.example.ledgercore.card.command.dto;

import com.example.ledgercore.common.currency.Currency;

import java.math.BigDecimal;

public record AuthorizeCardPaymentCommand(
        String reference,
        String pan,
        Short expiryMonth,
        Short expiryYear,
        String cvv,
        String merchantReference,
        BigDecimal amount,
        Currency currency
) {
}