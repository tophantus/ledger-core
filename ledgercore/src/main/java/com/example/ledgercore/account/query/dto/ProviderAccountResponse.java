package com.example.ledgercore.account.query.dto;

import com.example.ledgercore.account.enums.AccountStatus;
import com.example.ledgercore.common.currency.Currency;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ProviderAccountResponse(
        UUID providerId,

        UUID accountId,
        UUID productId,
        String accountNo,
        Currency currency,
        BigDecimal balance,
        BigDecimal holdAmount,
        AccountStatus status,
        Instant createdAt,
        Instant updatedAt,
        UUID ledgerAccountId
) {}