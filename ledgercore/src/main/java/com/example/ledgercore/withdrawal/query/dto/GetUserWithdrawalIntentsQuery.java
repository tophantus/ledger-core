package com.example.ledgercore.withdrawal.query.dto;

import com.example.ledgercore.withdrawal.enums.WithdrawalIntentStatus;

import java.util.UUID;

public record GetUserWithdrawalIntentsQuery(
        UUID userId,
        WithdrawalIntentStatus status,
        int page,
        int size
) {
    public GetUserWithdrawalIntentsQuery {
        page = Math.max(page, 0);
        size = Math.clamp(size, 1, 100);
    }
}