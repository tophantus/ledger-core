package com.example.ledgercore.reconciliation.command.port.outbound;

import java.util.List;
import java.util.UUID;

public interface JournalQueryPort {

    List<JournalReconciliationData> findByTransactionIds(
            List<UUID> transactionIds
    );
}