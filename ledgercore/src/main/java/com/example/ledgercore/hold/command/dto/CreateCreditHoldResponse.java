package com.example.ledgercore.hold.command.dto;

import com.example.ledgercore.common.currency.Currency;
import com.example.ledgercore.hold.enums.CreditHoldStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record CreateCreditHoldResponse(
        UUID holdId,
        UUID creditFacilityId,
        BigDecimal amount,
        Currency currency,
        CreditHoldStatus status,
        Instant expiresAt
) {
}