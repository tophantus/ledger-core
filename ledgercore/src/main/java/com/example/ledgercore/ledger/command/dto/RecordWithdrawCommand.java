package com.example.ledgercore.ledger.command.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record RecordWithdrawCommand(
        UUID transactionId,
        UUID sourceAccountId,
        BigDecimal amount,
        String currency
) {
}