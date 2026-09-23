package com.example.ledgercore.account.query.dto;

import com.example.ledgercore.account.enums.AccountStatus;
import com.example.ledgercore.common.currency.Currency;

import java.util.UUID;

public record AdminUserAccountFilter(
        String accountNo,
        AccountStatus status,
        Currency currency,
        UUID userId,
        int page,
        int size
) {
}