package com.example.ledgercore.account.query.dto;

import com.example.ledgercore.account.enums.AccountStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record AccountResponse(
        UUID id,
        UUID userId,
        String accountNo,
        String currency,
        BigDecimal balance,
        AccountStatus status,
        Instant createdAt,
        Instant updatedAt
) {
}