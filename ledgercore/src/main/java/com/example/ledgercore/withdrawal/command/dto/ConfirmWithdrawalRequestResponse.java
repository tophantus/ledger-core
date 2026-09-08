package com.example.ledgercore.withdrawal.command.dto;

import com.example.ledgercore.withdrawal.enums.WithdrawalRequestStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ConfirmWithdrawalRequestResponse(
        UUID requestId,
        WithdrawalRequestStatus requestStatus,
        UUID intentId,
        String withdrawalReference,
        BigDecimal amount,
        String currency,
        Instant intentExpiresAt
) {
}