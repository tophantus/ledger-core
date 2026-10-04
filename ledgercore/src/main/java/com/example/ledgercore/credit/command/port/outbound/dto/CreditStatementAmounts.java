package com.example.ledgercore.credit.command.port.outbound.dto;

import java.math.BigDecimal;

public record CreditStatementAmounts(
        BigDecimal purchasesAmount,
        BigDecimal paymentsAmount,
        BigDecimal feesAmount,
        BigDecimal accruedInterestAmount,
        BigDecimal postedInterestAmount
) {
}
