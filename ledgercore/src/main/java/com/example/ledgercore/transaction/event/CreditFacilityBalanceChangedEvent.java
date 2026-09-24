package com.example.ledgercore.transaction.event;

import com.example.ledgercore.common.currency.Currency;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record CreditFacilityBalanceChangedEvent(
        UUID transactionId,
        UUID creditFacilityId,
        BigDecimal balanceDelta,
        Currency currency,
        Instant changedAt
) {
}