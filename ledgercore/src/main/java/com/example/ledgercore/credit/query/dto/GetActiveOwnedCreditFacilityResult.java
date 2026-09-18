package com.example.ledgercore.credit.query.dto;

import com.example.ledgercore.common.currency.Currency;

import java.math.BigDecimal;
import java.util.UUID;

public record GetActiveOwnedCreditFacilityResult(
        UUID id,
        UUID customerId,
        UUID productId,
        BigDecimal creditLimit,
        BigDecimal outstandingBalance,
        Currency currency
) {
}