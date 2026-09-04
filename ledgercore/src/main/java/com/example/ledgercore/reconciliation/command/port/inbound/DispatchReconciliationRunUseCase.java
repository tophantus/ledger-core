package com.example.ledgercore.reconciliation.command.port.inbound;

import com.example.ledgercore.reconciliation.entity.ReconciliationRun;

public interface DispatchReconciliationRunUseCase {

    void execute(ReconciliationRun run);
}