package com.example.ledgercore.interest.command.port.inbound;

import com.example.ledgercore.interest.command.dto.InterestRunBatchResult;

import java.time.LocalDate;
import java.util.UUID;

public interface ProcessInterestAccrualBatchUseCase {

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
    ) implements InterestRunBatchResult {
    }
}