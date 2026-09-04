package com.example.ledgercore.reconciliation.command.port.inbound;

import com.example.ledgercore.reconciliation.entity.ReconciliationRun;

import java.time.Instant;

public interface ClaimReconciliationRunUseCase {

    ReconciliationRun execute(
            Instant now
    );
}