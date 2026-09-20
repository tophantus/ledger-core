package com.example.ledgercore.card.command.dto;

import com.example.ledgercore.card.enums.CardForm;
import com.example.ledgercore.card.enums.CardStatus;
import com.example.ledgercore.card.enums.CardType;

import java.time.Instant;
import java.util.UUID;

public record CreateDebitCardResult(
        UUID cardId,
        UUID accountId,
        CardType type,
        CardForm form,
        CardStatus status,
        String panLast4,
        Short expiryMonth,
        Short expiryYear,
        Instant issuedAt
) {
}