package com.example.ledgercore.credit.command.port.inbound;

import java.time.LocalDate;
import java.util.UUID;

public interface ProcessCreditOfferBatchUseCase {

    BatchResult execute(
            UUID runId,
            LocalDate businessDate,
            UUID lastProcessedId,
            int batchSize
    );

    record BatchResult(
            UUID lastProcessedId,
            boolean completed
    ) {
    }
}