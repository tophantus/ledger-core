package com.example.ledgercore.interest.command.port.outbound.credit;

import com.example.ledgercore.common.currency.Currency;

import java.util.UUID;

public record InterestEligibleCreditFacility(
        UUID creditFacilityId,
        UUID productId,
        Currency currency
) {
}