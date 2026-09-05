package com.example.ledgercore.ledger.query.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record JournalBalanceReconciliationData(
        UUID id,
        BigDecimal debitTotal,
        BigDecimal creditTotal,
        LocalDate businessDate
) {
}