package com.example.ledgercore.credit.command.service;

import java.time.LocalDate;
import java.util.UUID;

public interface ProcessCreditOfferBatchService {

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