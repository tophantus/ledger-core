package com.example.ledgercore.interest.command.port.outbound.credit;

import com.example.ledgercore.common.currency.Currency;

import java.math.BigDecimal;
import java.util.UUID;

public record InterestEligibleCreditFacility(
        UUID creditFacilityId,
        UUID productId,
        Currency currency,
        BigDecimal closingBalance
) {
}