package com.example.ledgercore.hold.command.dto;

import com.example.ledgercore.common.currency.Currency;
import com.example.ledgercore.hold.enums.AccountHoldStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record CreateAccountHoldResponse(
        UUID holdId,
        UUID accountId,
        BigDecimal amount,
        Currency currency,
        AccountHoldStatus status,
        Instant expiresAt
) {
}