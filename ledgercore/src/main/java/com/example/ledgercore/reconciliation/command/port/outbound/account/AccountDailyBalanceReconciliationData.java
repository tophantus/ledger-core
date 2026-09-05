package com.example.ledgercore.reconciliation.command.port.outbound.account;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record AccountDailyBalanceReconciliationData(
        UUID accountId,
        LocalDate businessDate,
        BigDecimal openingBalance,
        BigDecimal closingBalance
) {
}