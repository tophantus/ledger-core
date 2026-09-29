package com.example.ledgercore.credit.query.dto;

import com.example.ledgercore.common.currency.Currency;
import com.example.ledgercore.credit.enums.CreditFacilityStatus;
import com.example.ledgercore.credit.enums.CreditRepaymentMandateStatus;
import com.example.ledgercore.credit.enums.RepaymentType;

import java.time.Instant;
import java.util.UUID;

public record GetUserCreditFacilityResult(
        UUID id,
        UUID customerId,
        UUID productId,
        String creditLimit,
        String outstandingBalance,
        String holdAmount,
        String availableCredit,
        Currency currency,
        CreditFacilityStatus status,
        Instant openedAt,
        RepaymentMandateResult repaymentMandate
) {

    public record RepaymentMandateResult(
            UUID id,
            UUID accountId,
            RepaymentType repaymentType,
            CreditRepaymentMandateStatus status,
            Instant createdAt,
            Instant updatedAt
    ) {
    }
}