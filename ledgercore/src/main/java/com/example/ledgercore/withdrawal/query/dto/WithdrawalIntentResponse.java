package com.example.ledgercore.withdrawal.query.dto;

import com.example.ledgercore.withdrawal.enums.WithdrawalIntentStatus;

import java.time.Instant;
import java.util.UUID;

public record WithdrawalIntentResponse(
        UUID id,
        UUID accountId,
        String withdrawalReference,
        String amount,
        String currency,
        WithdrawalIntentStatus status,
        Instant expiresAt,
        Instant createdAt,
        Instant completedAt
) {
}