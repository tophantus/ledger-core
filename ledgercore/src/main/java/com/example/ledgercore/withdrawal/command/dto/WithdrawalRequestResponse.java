package com.example.ledgercore.withdrawal.command.dto;

import com.example.ledgercore.common.currency.Currency;
import com.example.ledgercore.withdrawal.enums.WithdrawalRequestStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record WithdrawalRequestResponse(
        UUID requestId,
        UUID accountId,
        BigDecimal amount,
        Currency currency,
        WithdrawalRequestStatus status,
        Instant expiresAt
) {
}