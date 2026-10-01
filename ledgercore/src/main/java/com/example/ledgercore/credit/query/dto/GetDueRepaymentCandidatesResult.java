package com.example.ledgercore.credit.query.dto;

import com.example.ledgercore.credit.enums.RepaymentType;

import java.util.UUID;

public record GetDueRepaymentCandidatesResult(
        UUID statementId,
        UUID creditFacilityId,
        UUID accountId,
        RepaymentType repaymentType
) {
}