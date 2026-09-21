package com.example.ledgercore.account.query.dto;

import com.example.ledgercore.account.enums.AccountStatus;
import com.example.ledgercore.common.currency.Currency;

import java.math.BigDecimal;
import java.util.UUID;

public record GetActiveOwnedAccountResult(
        UUID id,
        UUID userId,
        UUID productId,
        BigDecimal availableBalance,
        Currency currency,
        AccountStatus status
) {
}