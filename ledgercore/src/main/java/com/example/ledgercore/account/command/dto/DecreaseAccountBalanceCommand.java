package com.example.ledgercore.account.command.dto;

import com.example.ledgercore.common.currency.Currency;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record DecreaseAccountBalanceCommand(
        UUID accountId,
        BigDecimal amount,
        Currency currency,
        LocalDate businessDate
) {
}