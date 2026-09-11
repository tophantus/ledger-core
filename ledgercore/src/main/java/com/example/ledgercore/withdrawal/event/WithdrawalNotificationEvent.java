package com.example.ledgercore.withdrawal.event;

import com.example.ledgercore.common.currency.Currency;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record WithdrawalNotificationEvent(
        UUID withdrawalIntentId,
        UUID userId,
        String withdrawalLookupCode,
        BigDecimal amount,
        Currency currency,
        String encryptedWithdrawalCode,
        Instant expiresAt
) {
}