package com.example.ledgercore.withdrawal.command.dto;

import com.example.ledgercore.withdrawal.enums.WithdrawalRequestStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record WithdrawalRequestResponse(
        UUID requestId,
        UUID accountId,
        BigDecimal amount,
        String currency,
        WithdrawalRequestStatus status,
        Instant expiresAt
) {
}