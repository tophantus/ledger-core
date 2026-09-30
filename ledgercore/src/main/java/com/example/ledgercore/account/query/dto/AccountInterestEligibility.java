package com.example.ledgercore.account.query.dto;

import com.example.ledgercore.common.currency.Currency;

import java.math.BigDecimal;
import java.util.UUID;

public record AccountInterestEligibility(
        UUID accountId,
        UUID productId,
        Currency currency,
        BigDecimal closingBalance
) {
}