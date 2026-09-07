package com.example.ledgercore.transaction.command.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record PostInterestTransactionCommand(
        UUID accountId,
        BigDecimal amount,
        String currency,
        LocalDate businessDate
) {
}