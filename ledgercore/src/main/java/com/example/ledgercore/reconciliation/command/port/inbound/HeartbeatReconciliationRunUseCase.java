package com.example.ledgercore.reconciliation.command.port.inbound;

import java.time.Instant;
import java.util.UUID;

public interface HeartbeatReconciliationRunUseCase {

    void execute(
            UUID runId,
            Instant heartbeatAt
    );
}