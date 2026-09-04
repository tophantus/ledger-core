package com.example.ledgercore.reconciliation.command.port.inbound;

import java.time.LocalDate;
import java.util.UUID;

public interface ProcessTransactionJournalBatchUseCase {

    BatchResult execute(
            UUID runId,
            LocalDate businessDate,
            UUID lastProcessedId,
            long processedCount,
            int batchSize
    );

    record BatchResult(
            UUID lastProcessedId,
            long processedCount,
            boolean completed
    ) {
    }
}