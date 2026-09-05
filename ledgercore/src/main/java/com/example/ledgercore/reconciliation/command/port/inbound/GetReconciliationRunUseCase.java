package com.example.ledgercore.reconciliation.command.port.inbound;

import com.example.ledgercore.reconciliation.entity.ReconciliationRun;

import java.util.UUID;

public interface GetReconciliationRunUseCase {

    ReconciliationRun execute(UUID runId);
}
