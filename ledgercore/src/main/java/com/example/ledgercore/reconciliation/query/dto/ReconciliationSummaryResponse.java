package com.example.ledgercore.reconciliation.query.dto;

import java.time.LocalDate;
import java.util.List;

public record ReconciliationSummaryResponse(
        LocalDate businessDate,
        List<ReconciliationRunSummaryResponse> runs
) {
}