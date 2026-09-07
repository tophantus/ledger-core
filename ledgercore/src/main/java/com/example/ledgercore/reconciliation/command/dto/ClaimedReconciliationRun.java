package com.example.ledgercore.reconciliation.command.dto;

import com.example.ledgercore.reconciliation.enums.ReconciliationType;

import java.time.LocalDate;
import java.util.UUID;

public record ClaimedReconciliationRun(
        UUID runId,
        LocalDate businessDate,
        ReconciliationType type,
        UUID lastProcessedId,
        long processedCount
) {
}