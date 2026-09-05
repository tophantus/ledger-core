package com.example.ledgercore.reconciliation.command.port.outbound.account;

import java.math.BigDecimal;
import java.util.UUID;

public record AccountTransactionMovementReconciliationData(
        UUID accountId,
        BigDecimal totalCredit,
        BigDecimal totalDebit
) {
}