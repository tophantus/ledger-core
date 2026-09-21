package com.example.ledgercore.credit.command.dto;

import com.example.ledgercore.common.currency.Currency;
import com.example.ledgercore.credit.enums.CreditOfferStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record AcceptCreditOfferResult(
        UUID offerId,
        UUID creditFacilityId,
        UUID customerId,
        UUID productId,
        String creditLimit,
        Currency currency,
        CreditOfferStatus offerStatus,
        Instant acceptedAt
) {
}