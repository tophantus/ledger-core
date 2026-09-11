package com.example.ledgercore.transaction.event;

import com.example.ledgercore.common.currency.Currency;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record WithdrawCompletedEvent(
        UUID transactionId,
        String reference,
        UUID accountId,
        BigDecimal amount,
        Currency currency,
        Instant completedAt
) {
}