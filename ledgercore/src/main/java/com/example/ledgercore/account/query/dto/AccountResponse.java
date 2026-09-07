package com.example.ledgercore.account.query.dto;

import com.example.ledgercore.account.enums.AccountStatus;

import java.time.Instant;
import java.util.UUID;

public record AccountResponse(
        UUID id,
        UUID userId,
        String accountNo,
        UUID productId,
        String currency,
        String balance,
        AccountStatus status,
        Instant createdAt,
        Instant updatedAt
) {
}