package com.example.ledgercore.transaction.command.dto;

import com.example.ledgercore.transaction.enums.TransferIntentStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record CreateTransferIntentResult(

        UUID intentId,

        UUID sourceAccountId,

        UUID destinationAccountId,

        BigDecimal amount,

        String currency,

        String reference,

        TransferIntentStatus status,

        Instant expiresAt,

        Instant createdAt

) {
}