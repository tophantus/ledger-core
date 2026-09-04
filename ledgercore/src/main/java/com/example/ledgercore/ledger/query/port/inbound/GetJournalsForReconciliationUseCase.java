package com.example.ledgercore.ledger.query.port.inbound;

import com.example.ledgercore.ledger.query.dto.ReconciliationJournalData;

import java.util.List;
import java.util.UUID;

public interface GetJournalsForReconciliationUseCase {

    List<ReconciliationJournalData> execute(
            List<UUID> transactionIds
    );
}