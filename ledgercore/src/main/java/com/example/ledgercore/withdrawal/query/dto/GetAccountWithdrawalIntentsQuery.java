package com.example.ledgercore.withdrawal.query.dto;

import com.example.ledgercore.withdrawal.enums.WithdrawalIntentStatus;

import java.util.UUID;

public record GetAccountWithdrawalIntentsQuery(
        UUID userId,
        UUID accountId,
        WithdrawalIntentStatus status,
        int page,
        int size
) {
    public GetAccountWithdrawalIntentsQuery {
        page = Math.max(page, 0);
        size = Math.clamp(size, 1, 100);
    }
}