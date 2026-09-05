package com.example.ledgercore.ledger.query.port.inbound;

import com.example.ledgercore.ledger.query.dto.JournalBalanceReconciliationData;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface GetJournalsForBalanceReconciliationUseCase {

    List<JournalBalanceReconciliationData> execute(
            LocalDate businessDate,
            UUID lastProcessedId,
            int limit
    );
}