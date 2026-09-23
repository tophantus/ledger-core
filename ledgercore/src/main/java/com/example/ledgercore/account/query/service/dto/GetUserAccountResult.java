package com.example.ledgercore.account.query.service.dto;

import com.example.ledgercore.account.enums.AccountStatus;
import com.example.ledgercore.common.currency.Currency;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record GetUserAccountResult(
        UUID accountId,
        UUID userId,
        UUID productId,
        String accountNo,
        Currency currency,
        BigDecimal balance,
        BigDecimal holdAmount,
        AccountStatus status,
        Long version,
        UUID ledgerAccountId,
        Instant createdAt,
        Instant updatedAt
) {
    public BigDecimal getAvailableBalance() {
        return balance.subtract(holdAmount);
    }
}