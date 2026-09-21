package com.example.ledgercore.card.command.dto;

import com.example.ledgercore.card.enums.CardAuthorizationStatus;
import com.example.ledgercore.common.currency.Currency;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record AuthorizeCardPaymentResult(
        UUID authorizationId,
        UUID cardId,
        String reference,
        CardAuthorizationStatus status,
        BigDecimal amount,
        Currency currency,
        Instant authorizedAt,
        Instant expiresAt
) {
}