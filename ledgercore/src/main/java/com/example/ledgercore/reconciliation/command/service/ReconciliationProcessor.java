package com.example.ledgercore.reconciliation.command.service;

import com.example.ledgercore.reconciliation.entity.ReconciliationRun;
import com.example.ledgercore.reconciliation.enums.ReconciliationType;

public interface ReconciliationProcessor {

    ReconciliationType getType();

    void process(ReconciliationRun run);
}