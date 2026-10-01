package com.example.ledgercore.credit.command.dto;

import com.example.ledgercore.credit.enums.RepaymentType;

import java.util.UUID;

public record CreateCreditRepaymentMandateCommand(
        UUID userId,
        UUID creditFacilityId,
        UUID accountId,
        RepaymentType repaymentType
) {
}