package com.example.ledgercore.transaction.query.port.inbound;

import com.example.ledgercore.transaction.query.dto.ReconciliationTransactionData;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface GetTransactionsForReconciliationUseCase {

    List<ReconciliationTransactionData> execute(
            LocalDate businessDate,
            UUID lastProcessedId,
            int limit
    );
}