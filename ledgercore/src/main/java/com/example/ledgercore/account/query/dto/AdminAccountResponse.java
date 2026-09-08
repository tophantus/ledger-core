package com.example.ledgercore.account.query.dto;

import com.example.ledgercore.account.enums.AccountStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record AdminAccountResponse(
        UUID id,
        UUID userId,
        String accountNo,
        UUID productId,
        String currency,
        BigDecimal balance,
        AccountStatus status,
        UUID ledgerAccountId,
        Instant createdAt,
        Instant updatedAt
) {
}