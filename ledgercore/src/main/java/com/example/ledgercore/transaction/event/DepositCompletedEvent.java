package com.example.ledgercore.transaction.event;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record DepositCompletedEvent(
        UUID transactionId,
        String reference,
        UUID accountId,
        BigDecimal amount,
        String currency,
        Instant completedAt
) {
}