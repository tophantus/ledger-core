package com.example.ledgercore.ledger.command.dto;

import com.example.ledgercore.common.currency.Currency;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record RecordInterestPostingCommand(
        UUID transactionId,
        UUID accountId,
        BigDecimal amount,
        Currency currency,
        LocalDate businessDate
) {
}