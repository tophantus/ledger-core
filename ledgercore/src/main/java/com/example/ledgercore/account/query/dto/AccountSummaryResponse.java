package com.example.ledgercore.account.query.dto;

import com.example.ledgercore.account.enums.AccountStatus;
import com.example.ledgercore.common.currency.Currency;

import java.util.UUID;

public record AccountSummaryResponse(
        UUID id,
        String accountNo,
        UUID productId,
        Currency currency,
        String balance,
        String holdAmount,
        String availableBalance,
        AccountStatus status
) {
}