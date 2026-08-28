package com.example.ledgercore.transaction.query.dto;

import java.math.BigDecimal;

public record TransferIntentNotificationInfo(
        String destinationAccountNo,
        BigDecimal amount,
        String currency,
        String reference,
        String description
) {
}