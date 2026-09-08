package com.example.ledgercore.account.query.dto;

import com.example.ledgercore.account.enums.AccountStatus;

import java.time.Instant;
import java.util.UUID;

public record AdminAccountResponse(
        UUID id,
        UUID userId,
        String accountNo,
        UUID productId,
        String currency,
        String balance,
        String holdAmount,
        String availableBalance,
        AccountStatus status,
        UUID ledgerAccountId,
        Instant createdAt,
        Instant updatedAt
) {
}