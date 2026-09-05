package com.example.ledgercore.reconciliation.command.port.inbound;

import com.example.ledgercore.reconciliation.enums.ReconciliationErrorCode;
import com.example.ledgercore.reconciliation.enums.ReconciliationTargetType;

import java.util.UUID;

public interface RecordReconciliationExceptionUseCase {

    void execute(
            UUID reconciliationRunId,
            ReconciliationTargetType targetType,
            UUID targetId,
            ReconciliationErrorCode errorCode,
            String expectedValue,
            String actualValue,
            String message
    );
}