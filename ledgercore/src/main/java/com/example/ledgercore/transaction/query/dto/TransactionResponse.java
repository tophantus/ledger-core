package com.example.ledgercore.transaction.query.dto;

import com.example.ledgercore.common.currency.Currency;
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
        Currency currency,
        String description,
        Boolean incoming,
        Instant createdAt,
        Instant completedAt
) {
}