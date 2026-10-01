package com.example.ledgercore.credit.query.dto;

import com.example.ledgercore.credit.enums.CreditStatementStatus;

import java.time.LocalDate;
import java.util.UUID;

public record GetCreditStatementResponse(
        UUID id,
        UUID creditFacilityId,
        LocalDate periodStart,
        LocalDate periodEnd,
        LocalDate statementDate,
        LocalDate dueDate,
        String openingBalance,
        String purchasesAmount,
        String paymentsAmount,
        String feesAmount,
        String interestAmount,
        String closingBalance,
        String minimumPayment,
        String paidAmount,
        CreditStatementStatus status
) {
}