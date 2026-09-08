package com.example.ledgercore.account.query.dto;

import com.example.ledgercore.account.enums.AccountStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record AdminAccountDetailResponse(
        UUID id,
        String accountNo,
        UUID productId,
        String currency,
        BigDecimal balance,
        AccountStatus status,
        UUID ledgerAccountId,
        Instant createdAt,
        Instant updatedAt,
        UserInfo user
) {

    public record UserInfo(
            UUID id,
            String email,
            String fullName,
            String avatarUrl
    ) {
    }
}