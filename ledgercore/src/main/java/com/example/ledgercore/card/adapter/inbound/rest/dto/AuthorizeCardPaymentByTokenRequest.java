package com.example.ledgercore.card.adapter.inbound.rest.dto;

import com.example.ledgercore.common.currency.Currency;

import java.math.BigDecimal;

public record AuthorizeCardPaymentByTokenRequest(
        String token,
        String reference,
        String merchantReference,
        BigDecimal amount,
        Currency currency
) {
}