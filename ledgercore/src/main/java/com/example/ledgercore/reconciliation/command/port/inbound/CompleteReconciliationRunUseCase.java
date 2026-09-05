package com.example.ledgercore.reconciliation.command.port.inbound;

import java.time.Instant;
import java.util.UUID;

public interface CompleteReconciliationRunUseCase {

    void execute(
            UUID runId,
            Instant completedAt
    );
}