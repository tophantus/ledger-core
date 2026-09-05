package com.example.ledgercore.transaction.query.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record AccountTransactionMovementData(
        UUID accountId,
        BigDecimal totalCredit,
        BigDecimal totalDebit
) {
}