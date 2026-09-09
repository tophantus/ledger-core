package com.example.ledgercore.withdrawal.command.dto;

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
        String currency,
        Instant executedAt
) {
}