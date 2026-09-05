package com.example.ledgercore.reconciliation.command.port.outbound.transaction;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record TransactionReconciliationData(
        UUID id,
        BigDecimal amount,
        LocalDate businessDate
) {
}