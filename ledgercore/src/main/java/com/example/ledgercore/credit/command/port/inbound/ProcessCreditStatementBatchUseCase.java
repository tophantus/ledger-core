package com.example.ledgercore.credit.command.port.inbound;

import java.time.LocalDate;
import java.util.UUID;

public interface ProcessCreditStatementBatchUseCase {

    BatchResult execute(
            UUID runId,
            LocalDate periodStart,
            LocalDate periodEnd,
            LocalDate statementDate,
            LocalDate dueDate,
            UUID lastProcessedFacilityId,
            int batchSize
    );

    record BatchResult(
            UUID lastProcessedFacilityId,
            boolean completed
    ) {
    }
}