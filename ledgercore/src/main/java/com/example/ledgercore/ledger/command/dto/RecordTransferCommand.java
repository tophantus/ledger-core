package com.example.ledgercore.ledger.command.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record RecordTransferCommand(
        UUID transactionId,
        UUID sourceAccountId,
        UUID destinationAccountId,
        BigDecimal amount,
        String currency,
        LocalDate businessDate
) {
}