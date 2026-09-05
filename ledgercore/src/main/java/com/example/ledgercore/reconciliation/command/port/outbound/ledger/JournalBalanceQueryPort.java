package com.example.ledgercore.reconciliation.command.port.outbound.ledger;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface JournalBalanceQueryPort {

    List<JournalBalanceReconciliationData> findBatch(
            LocalDate businessDate,
            UUID lastProcessedId,
            int limit
    );
}