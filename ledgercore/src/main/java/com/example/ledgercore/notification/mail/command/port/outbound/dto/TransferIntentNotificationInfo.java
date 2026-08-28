package com.example.ledgercore.notification.mail.command.port.outbound.dto;

import java.math.BigDecimal;

public record TransferIntentNotificationInfo(
        String destinationAccountNo,
        BigDecimal amount,
        String currency,
        String reference,
        String description
) {
}