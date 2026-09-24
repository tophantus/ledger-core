package com.example.ledgercore.card.command.dto;

import com.example.ledgercore.card.enums.CardAuthorizationStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record CaptureCardPaymentResult(
        UUID captureId,
        UUID authorizationId,
        UUID transactionId,
        CardAuthorizationStatus authorizationStatus,
        BigDecimal amount,
        Instant capturedAt
) {
}
