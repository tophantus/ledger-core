package com.example.ledgercore.account.query.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record AccountDailyBalanceResponse(
        UUID accountId,
        LocalDate businessDate,
        BigDecimal closingBalance
) {
}