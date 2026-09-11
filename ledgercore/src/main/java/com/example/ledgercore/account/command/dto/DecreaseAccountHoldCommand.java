package com.example.ledgercore.account.command.dto;

import com.example.ledgercore.common.currency.Currency;

import java.math.BigDecimal;
import java.util.UUID;

public record DecreaseAccountHoldCommand(
        UUID accountId,
        BigDecimal amount,
        Currency currency
) {
}