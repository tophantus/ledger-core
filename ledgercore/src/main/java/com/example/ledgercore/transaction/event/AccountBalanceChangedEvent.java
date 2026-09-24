package com.example.ledgercore.transaction.event;

import com.example.ledgercore.common.currency.Currency;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record AccountBalanceChangedEvent(
        UUID transactionId,
        UUID accountId,
        BigDecimal balanceDelta,
        Currency currency,
        Instant changedAt
) {
}