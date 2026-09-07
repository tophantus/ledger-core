package com.example.ledgercore.ledger.command.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record RecordInterestAccrualJournalCommand(
        UUID accrualId,
        LocalDate businessDate,
        String currency,
        BigDecimal amount
) {
}