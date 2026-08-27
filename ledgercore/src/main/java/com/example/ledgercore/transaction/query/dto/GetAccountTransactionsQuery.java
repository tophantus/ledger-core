package com.example.ledgercore.transaction.query.dto;

import com.example.ledgercore.transaction.enums.TransactionStatus;
import com.example.ledgercore.transaction.enums.TransactionType;

import java.time.Instant;
import java.util.UUID;

public record GetAccountTransactionsQuery(
        UUID accountId,
        TransactionStatus status,
        TransactionType type,
        String currency,
        Instant from,
        Instant to,
        int page,
        int size
) {
}