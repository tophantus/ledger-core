package com.example.ledgercore.card.query.dto;

import java.util.UUID;

public record RevealCardDetailsQuery(
        UUID customerId,
        UUID cardId,
        String pin
) {
}