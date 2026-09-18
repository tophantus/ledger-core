package com.example.ledgercore.credit.command.service.dto;

import com.example.ledgercore.common.currency.Currency;
import com.example.ledgercore.credit.enums.CreditFacilityStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record CreateCreditFacilityResult(
        UUID facilityId,
        UUID customerId,
        UUID productId,
        BigDecimal creditLimit,
        BigDecimal outstandingBalance,
        Currency currency,
        CreditFacilityStatus status,
        Instant openedAt
) {
}