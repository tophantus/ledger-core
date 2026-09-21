package com.example.ledgercore.card.command.port.outbound.dto;

import com.example.ledgercore.common.currency.Currency;
import com.example.ledgercore.credit.enums.CreditFacilityStatus;

import java.math.BigDecimal;
import java.util.UUID;

public record CardCreditFacilityInfo(
        UUID id,
        UUID customerId,
        UUID productId,
        BigDecimal creditLimit,
        BigDecimal outstandingBalance,
        Currency currency,
        CreditFacilityStatus status
) {
}