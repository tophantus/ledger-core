package com.example.ledgercore.reconciliation.query.port.inbound;

import com.example.ledgercore.reconciliation.query.dto.ReconciliationRunSummaryResponse;

import java.time.LocalDate;
import java.util.List;

public interface GetReconciliationSummaryUseCase {

    List<ReconciliationRunSummaryResponse> execute(
            LocalDate businessDate
    );
}