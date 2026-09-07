package com.example.ledgercore.reconciliation.command.service;

import com.example.ledgercore.reconciliation.command.dto.ClaimedReconciliationRun;

public interface ReconciliationDispatcher {

    void dispatch(ClaimedReconciliationRun run);
}