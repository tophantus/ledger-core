package com.example.ledgercore.withdrawal.event;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record WithdrawalNotificationEvent(
        UUID withdrawalIntentId,
        UUID userId,
        String withdrawalLookupCode,
        BigDecimal amount,
        String currency,
        String encryptedWithdrawalCode,
        Instant expiresAt
) {
}