package com.example.ledgercore.card.query.dto;

import com.example.ledgercore.card.enums.CardForm;
import com.example.ledgercore.card.enums.CardStatus;
import com.example.ledgercore.card.enums.CardType;

import java.time.Instant;
import java.util.UUID;

public record CardInfo(
        UUID cardId,
        UUID customerId,
        CardType type,
        CardForm form,
        CardStatus status,
        UUID accountId,
        UUID creditFacilityId,
        String panLast4,
        Short expiryMonth,
        Short expiryYear,
        Instant issuedAt,
        Instant activatedAt,
        Instant closedAt
) {
}