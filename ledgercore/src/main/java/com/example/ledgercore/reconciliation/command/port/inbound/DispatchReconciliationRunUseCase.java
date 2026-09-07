package com.example.ledgercore.reconciliation.command.port.inbound;

import com.example.ledgercore.reconciliation.command.dto.ClaimedReconciliationRun;

public interface DispatchReconciliationRunUseCase {

    void execute(ClaimedReconciliationRun run);
}