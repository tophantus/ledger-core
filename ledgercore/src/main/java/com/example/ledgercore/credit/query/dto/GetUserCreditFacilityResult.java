package com.example.ledgercore.credit.query.dto;

import com.example.ledgercore.common.currency.Currency;
import com.example.ledgercore.credit.enums.CreditFacilityStatus;

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
        Instant openedAt
) {
}