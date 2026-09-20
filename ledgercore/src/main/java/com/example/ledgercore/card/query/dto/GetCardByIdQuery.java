package com.example.ledgercore.card.query.dto;

import java.util.UUID;

public record GetCardByIdQuery(
        UUID customerId,
        UUID cardId
) {
}