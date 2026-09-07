package com.example.ledgercore.reconciliation.query.port.inbound;

import com.example.ledgercore.reconciliation.query.dto.ReconciliationSummaryResponse;

import java.time.LocalDate;

public interface GetReconciliationSummaryUseCase {

    ReconciliationSummaryResponse execute(
            LocalDate businessDate
    );
}