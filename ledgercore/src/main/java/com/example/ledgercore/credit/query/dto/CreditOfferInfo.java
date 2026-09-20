package com.example.ledgercore.credit.query.dto;

import com.example.ledgercore.common.currency.Currency;
import com.example.ledgercore.credit.enums.CreditOfferStatus;

import java.time.Instant;
import java.util.UUID;

public record CreditOfferInfo(
        UUID offerId,
        UUID customerId,
        UUID creditFacilityId,
        UUID productId,
        String approvedLimit,
        Currency currency,
        CreditOfferStatus status,
        Instant expiresAt,
        Instant createdAt
) {
}