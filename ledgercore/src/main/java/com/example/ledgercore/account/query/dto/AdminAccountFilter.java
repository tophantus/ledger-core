package com.example.ledgercore.account.query.dto;

import com.example.ledgercore.account.enums.AccountStatus;

import java.util.UUID;

public record AdminAccountFilter(
        String accountNo,
        AccountStatus status,
        String currency,
        UUID userId,
        int page,
        int size
) {
}