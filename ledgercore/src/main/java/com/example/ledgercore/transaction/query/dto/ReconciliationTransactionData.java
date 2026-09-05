package com.example.ledgercore.transaction.query.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record ReconciliationTransactionData(
        UUID id,
        BigDecimal amount,
        LocalDate businessDate
) {
}