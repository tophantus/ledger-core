package com.example.ledgercore.credit.adapter.inbound.rest.dto;

import com.example.ledgercore.credit.enums.RepaymentType;

import java.util.UUID;

public record CreateCreditRepaymentMandateRequest(
        UUID creditFacilityId,
        UUID accountId,
        RepaymentType repaymentType
) {
}