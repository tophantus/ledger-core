package com.example.ledgercore.reconciliation.command.port.outbound.ledger;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record JournalReconciliationData(
        UUID id,
        UUID transactionId,
        BigDecimal debitTotal,
        BigDecimal creditTotal,
        LocalDate businessDate
) {
}