package com.example.ledgercore.transaction.query.dto;

import com.example.ledgercore.common.currency.Currency;

import java.math.BigDecimal;

public record TransferIntentNotificationInfo(
        String destinationAccountNo,
        BigDecimal amount,
        Currency currency,
        String reference,
        String description
) {
}