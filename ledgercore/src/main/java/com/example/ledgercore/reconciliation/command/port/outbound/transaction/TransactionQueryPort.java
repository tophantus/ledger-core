package com.example.ledgercore.reconciliation.command.port.outbound.transaction;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface TransactionQueryPort {

    List<TransactionReconciliationData> findBatch(
            LocalDate businessDate,
            UUID lastProcessedId,
            int limit
    );
}