package com.example.ledgercore.credit.command.service.dto;

import com.example.ledgercore.common.currency.Currency;
import com.example.ledgercore.credit.enums.CreditOfferStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record CreateCreditOfferResult(
        UUID offerId,
        UUID customerId,
        UUID creditFacilityId,
        UUID productId,
        BigDecimal approvedLimit,
        Currency currency,
        CreditOfferStatus status,
        Instant expiresAt,
        Instant createdAt
) {
}