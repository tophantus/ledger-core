package com.example.ledgercore.card.query.dto;

import java.util.UUID;

public record GetUserCardsQuery(
        UUID customerId,
        int page,
        int size
) {
}