package com.example.ledgercore.reconciliation.command.service;

import com.example.ledgercore.reconciliation.command.dto.ClaimedReconciliationRun;
import com.example.ledgercore.reconciliation.enums.ReconciliationType;

public interface ReconciliationProcessor {

    ReconciliationType getType();

    void process(ClaimedReconciliationRun run);
}