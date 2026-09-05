package com.example.ledgercore.reconciliation.query.repository;

import com.example.ledgercore.reconciliation.enums.ReconciliationRunStatus;
import com.example.ledgercore.reconciliation.enums.ReconciliationType;

import java.util.UUID;

public interface ReconciliationRunSummaryProjection {

    UUID getId();

    ReconciliationType getType();

    ReconciliationRunStatus getStatus();

    long getProcessedCount();
}