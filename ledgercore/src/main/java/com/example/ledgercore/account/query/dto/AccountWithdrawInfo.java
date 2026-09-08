package com.example.ledgercore.account.query.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record AccountWithdrawInfo(
        UUID accountId,
        UUID userId,
        String currency,
        BigDecimal availableBalance
) {
}