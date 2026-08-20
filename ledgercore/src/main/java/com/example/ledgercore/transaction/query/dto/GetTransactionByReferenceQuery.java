package com.example.ledgercore.transaction.query.dto;

import java.util.UUID;

public record GetTransactionByReferenceQuery(
        UUID userId,
        String reference
) {
}