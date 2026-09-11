package com.example.ledgercore.interest.command.port.inbound;

import com.example.ledgercore.interest.command.dto.InterestRunBatchResult;

import java.time.LocalDate;
import java.util.UUID;

public interface ProcessInterestPostingBatchUseCase {

    BatchResult execute(
            UUID runId,
            LocalDate periodStart,
            LocalDate periodEnd,
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