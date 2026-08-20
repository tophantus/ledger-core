package com.example.ledgercore.transaction.query.dto;

import com.example.ledgercore.transaction.enums.TransactionStatus;
import com.example.ledgercore.transaction.enums.TransactionType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record TransactionResponse(
        UUID id,
        String reference,
        TransactionType type,
        TransactionStatus status,
        UUID sourceAccountId,
        UUID destinationAccountId,
        BigDecimal amount,
        String currency,
        String description,
        Instant createdAt,
        Instant completedAt
) {
}