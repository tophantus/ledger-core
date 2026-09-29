package com.example.ledgercore.credit.adapter.inbound.rest.dto;

import com.example.ledgercore.credit.enums.RepaymentType;

public record UpdateCreditRepaymentMandateRequest(
        RepaymentType repaymentType
) {
}