package com.example.ledgercore.credit.command.dto;

import com.example.ledgercore.credit.enums.CreditRepaymentMandateStatus;

import java.time.Instant;
import java.util.UUID;

public record RevokeCreditRepaymentMandateResult(
        UUID mandateId,
        UUID creditFacilityId,
        UUID accountId,
        CreditRepaymentMandateStatus status,
        Instant revokedAt
) {
}