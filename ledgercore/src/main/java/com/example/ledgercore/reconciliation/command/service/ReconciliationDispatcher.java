package com.example.ledgercore.reconciliation.command.service;

import com.example.ledgercore.reconciliation.entity.ReconciliationRun;

public interface ReconciliationDispatcher {

    void dispatch(ReconciliationRun run);
}