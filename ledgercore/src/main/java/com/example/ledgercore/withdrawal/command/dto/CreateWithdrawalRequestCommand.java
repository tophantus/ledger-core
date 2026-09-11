package com.example.ledgercore.withdrawal.command.dto;

import com.example.ledgercore.common.currency.Currency;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateWithdrawalRequestCommand(
        UUID userId,
        UUID accountId,
        BigDecimal amount,
        Currency currency
) {
}