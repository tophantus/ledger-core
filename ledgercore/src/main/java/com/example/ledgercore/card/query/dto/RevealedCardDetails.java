package com.example.ledgercore.card.query.dto;

import java.util.UUID;

public record RevealedCardDetails(
        UUID cardId,
        String pan,
        String cvv
) {
}