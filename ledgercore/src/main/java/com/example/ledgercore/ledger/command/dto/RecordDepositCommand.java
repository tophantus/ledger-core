package com.example.ledgercore.ledger.command.dto;

import com.example.ledgercore.common.currency.Currency;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record RecordDepositCommand(
        UUID transactionId,
        UUID destinationAccountId,
        BigDecimal amount,
        Currency currency,
        LocalDate businessDate
) {
}