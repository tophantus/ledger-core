package com.example.ledgercore.withdrawal.event;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record WithdrawalNotificationEvent(
        UUID withdrawalIntentId,
        UUID userId,
        String withdrawalReference,
        BigDecimal amount,
        String currency,
        String encryptedWithdrawalCode,
        Instant expiresAt
) {
}