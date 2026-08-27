package com.example.ledgercore.transaction.query.dto;

import java.util.UUID;

public record GetTransactionQuery(
        UUID userId,
        UUID transactionId
) {
}