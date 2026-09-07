package com.example.ledgercore.ledger.command.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record RecordInterestPostingCommand(
        UUID transactionId,
        UUID accountId,
        BigDecimal amount,
        String currency,
        LocalDate businessDate
) {
}