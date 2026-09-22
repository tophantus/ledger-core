package com.example.ledgercore.card.command.dto;

import com.example.ledgercore.common.currency.Currency;

import java.math.BigDecimal;

public record AuthorizeCardPaymentByTokenCommand(
        String reference,
        String providerClientId,
        String providerCredential,
        String token,
        String merchantReference,
        BigDecimal amount,
        Currency currency
) {
}
