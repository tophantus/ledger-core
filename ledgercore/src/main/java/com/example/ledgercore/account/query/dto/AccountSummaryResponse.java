package com.example.ledgercore.account.query.dto;

import com.example.ledgercore.account.enums.AccountStatus;

import java.util.UUID;

public record AccountSummaryResponse(
        UUID id,
        String accountNo,
        UUID productId,
        String currency,
        String balance,
        String holdAmount,
        String availableBalance,
        AccountStatus status
) {
}