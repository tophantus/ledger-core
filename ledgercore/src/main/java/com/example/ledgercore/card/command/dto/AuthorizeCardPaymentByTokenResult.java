package com.example.ledgercore.card.command.dto;

import com.example.ledgercore.card.enums.CardAuthorizationStatus;
import com.example.ledgercore.common.currency.Currency;

import java.time.Instant;
import java.util.UUID;

public record AuthorizeCardPaymentByTokenResult(
        UUID authorizationId,
        UUID cardId,
        String reference,
        CardAuthorizationStatus status,
        String amount,
        Currency currency,
        Instant authorizedAt,
        Instant expiresAt
) {
}
