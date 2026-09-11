package com.example.ledgercore.account.query.dto;

import com.example.ledgercore.common.currency.Currency;

import java.util.UUID;

public record AccountInterestEligibility(
        UUID accountId,
        UUID productId,
        Currency currency
) {
}