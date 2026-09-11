package com.example.ledgercore.withdrawal.command.dto;

import com.example.ledgercore.common.currency.Currency;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ExecuteWithdrawalResponse(
        UUID executionId,
        UUID withdrawalIntentId,
        UUID transactionId,
        UUID atmTerminalId,
        String withdrawalReference,
        BigDecimal amount,
        Currency currency,
        Instant executedAt
) {
}