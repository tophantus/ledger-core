package com.example.ledgercore.interest.command.port.outbound;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record AccountDailyBalanceInfo(
        UUID accountId,
        LocalDate businessDate,
        BigDecimal closingBalance
) {
}