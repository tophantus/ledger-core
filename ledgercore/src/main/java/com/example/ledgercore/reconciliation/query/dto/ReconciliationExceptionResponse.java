package com.example.ledgercore.reconciliation.query.dto;

import com.example.ledgercore.reconciliation.enums.ReconciliationErrorCode;
import com.example.ledgercore.reconciliation.enums.ReconciliationTargetType;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record ReconciliationExceptionResponse(
        UUID id,
        UUID reconciliationRunId,
        LocalDate businessDate,
        ReconciliationTargetType targetType,
        UUID targetId,
        ReconciliationErrorCode errorCode,
        String expectedValue,
        String actualValue,
        String message,
        Instant createdAt
) {
}