package com.example.ledgercore.credit.command.dto;

import com.example.ledgercore.credit.enums.RepaymentType;

import java.util.UUID;

public record UpdateCreditRepaymentMandateCommand(
        UUID userId,
        UUID mandateId,
        UUID accountId,
        RepaymentType repaymentType
) {
}