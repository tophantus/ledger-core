package com.example.ledgercore.withdrawal.command.dto;

import com.example.ledgercore.common.currency.Currency;
import com.example.ledgercore.withdrawal.enums.WithdrawalRequestStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ConfirmWithdrawalRequestResponse(
        UUID requestId,
        WithdrawalRequestStatus requestStatus,
        UUID intentId,
        String withdrawalReference,
        String amount,
        Currency currency,
        Instant intentExpiresAt
) {
}