package com.example.ledgercore.account.command.dto;

import com.example.ledgercore.common.currency.Currency;

import java.math.BigDecimal;
import java.util.UUID;

public record IncreaseAccountHoldCommand(
        UUID accountId,
        BigDecimal amount,
        Currency currency
) {
}