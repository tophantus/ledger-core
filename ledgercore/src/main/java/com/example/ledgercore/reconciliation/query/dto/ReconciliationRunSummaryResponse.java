package com.example.ledgercore.reconciliation.query.dto;

import com.example.ledgercore.reconciliation.enums.ReconciliationRunStatus;
import com.example.ledgercore.reconciliation.enums.ReconciliationType;

import java.util.UUID;

public record ReconciliationRunSummaryResponse(
        UUID id,
        ReconciliationType type,
        ReconciliationRunStatus status,
        long processedCount
) {
}