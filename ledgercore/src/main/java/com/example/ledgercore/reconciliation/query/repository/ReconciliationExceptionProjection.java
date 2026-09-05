package com.example.ledgercore.reconciliation.query.repository;

import com.example.ledgercore.reconciliation.enums.ReconciliationErrorCode;
import com.example.ledgercore.reconciliation.enums.ReconciliationTargetType;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public interface ReconciliationExceptionProjection {

    UUID getId();

    UUID getReconciliationRunId();

    LocalDate getBusinessDate();

    ReconciliationTargetType getTargetType();

    UUID getTargetId();

    ReconciliationErrorCode getErrorCode();

    String getExpectedValue();

    String getActualValue();

    String getMessage();

    Instant getCreatedAt();
}