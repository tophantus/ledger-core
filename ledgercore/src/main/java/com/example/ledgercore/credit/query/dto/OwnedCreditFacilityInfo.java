package com.example.ledgercore.credit.query.dto;

import com.example.ledgercore.common.currency.Currency;
import com.example.ledgercore.credit.enums.CreditFacilityStatus;

import java.math.BigDecimal;
import java.util.UUID;

public record OwnedCreditFacilityInfo(
        UUID id,
        UUID customerId,
        UUID productId,
        BigDecimal availableCredit,
        Currency currency,
        CreditFacilityStatus status
) {
}