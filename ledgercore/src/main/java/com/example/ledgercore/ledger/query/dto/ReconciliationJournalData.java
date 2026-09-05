package com.example.ledgercore.ledger.query.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record ReconciliationJournalData(
        UUID id,
        UUID transactionId,
        BigDecimal debitTotal,
        BigDecimal creditTotal,
        LocalDate businessDate
) {
}