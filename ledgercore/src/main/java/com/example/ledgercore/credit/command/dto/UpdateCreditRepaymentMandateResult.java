package com.example.ledgercore.credit.command.dto;

import com.example.ledgercore.credit.enums.CreditRepaymentMandateStatus;
import com.example.ledgercore.credit.enums.RepaymentType;

import java.time.Instant;
import java.util.UUID;

public record UpdateCreditRepaymentMandateResult(
        UUID mandateId,
        UUID creditFacilityId,
        UUID accountId,
        RepaymentType repaymentType,
        CreditRepaymentMandateStatus status,
        Instant updatedAt
) {
}