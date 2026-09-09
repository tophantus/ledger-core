package com.example.ledgercore.withdrawal.command.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateWithdrawalRequestCommand(
        UUID userId,
        UUID accountId,
        BigDecimal amount,
        String currency
) {
}