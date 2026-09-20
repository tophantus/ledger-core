package com.example.ledgercore.credit.command.service.dto;

import com.example.ledgercore.common.currency.Currency;

import java.math.BigDecimal;
import java.util.UUID;

public record CreditOfferEvaluationResult(
        UUID customerId,
        UUID creditFacilityId,
        UUID productId,
        BigDecimal approvedLimit,
        Currency currency
) {
}