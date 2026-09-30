package com.example.ledgercore.credit.query.dto;

import com.example.ledgercore.common.currency.Currency;

import java.util.UUID;

public record CreditFacilityInterestEligibility(
        UUID creditFacilityId,
        UUID productId,
        Currency currency
) {
}
