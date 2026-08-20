package com.example.ledgercore.ledger.command.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record RecordDepositCommand(
        UUID transactionId,
        UUID destinationAccountId,
        BigDecimal amount,
        String currency
) {
}