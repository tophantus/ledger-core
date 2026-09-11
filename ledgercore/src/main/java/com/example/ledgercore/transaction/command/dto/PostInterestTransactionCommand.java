package com.example.ledgercore.transaction.command.dto;

import com.example.ledgercore.common.currency.Currency;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record PostInterestTransactionCommand(
        UUID accountId,
        BigDecimal amount,
        Currency currency,
        LocalDate businessDate
) {
}