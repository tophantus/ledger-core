package com.example.ledgercore.notification.mail.command.port.outbound.dto;

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