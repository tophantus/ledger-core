package com.example.ledgercore.reconciliation.command.service;

import com.example.ledgercore.reconciliation.command.dto.ClaimedReconciliationRun;
import com.example.ledgercore.reconciliation.command.port.inbound.CompleteReconciliationRunUseCase;
import com.example.ledgercore.reconciliation.command.port.inbound.HeartbeatReconciliationRunUseCase;
import com.example.ledgercore.reconciliation.command.port.inbound.ProcessTransactionJournalBatchUseCase;
import com.example.ledgercore.reconciliation.command.port.inbound.ProcessTransactionJournalBatchUseCase.BatchResult;
import com.example.ledgercore.reconciliation.config.ReconciliationRunProperties;
import com.example.ledgercore.reconciliation.enums.ReconciliationType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionJournalProcessor
        implements ReconciliationProcessor {

    private final ProcessTransactionJournalBatchUseCase
            processBatchUseCase;

    private final HeartbeatReconciliationRunUseCase
            heartbeatUseCase;

    private final CompleteReconciliationRunUseCase
            completeRunUseCase;

    private final ReconciliationRunProperties reconciliationRunProperties;

    @Override
    public ReconciliationType getType() {
        return ReconciliationType.TRANSACTION_JOURNAL;
    }

    @Override
    public void process(ClaimedReconciliationRun run) {

        UUID runId = run.runId();
        UUID lastProcessedId = run.lastProcessedId();
        long processedCount = run.processedCount();

        log.info(
                "Starting transaction journal reconciliation: runId={}, businessDate={}, lastProcessedId={}, processedCount={}",
                runId,
                run.businessDate(),
                lastProcessedId,
                processedCount
        );

        while (true) {

            BatchResult result =
                    processBatchUseCase.execute(
                            runId,
                            run.businessDate(),
                            lastProcessedId,
                            processedCount,
                            reconciliationRunProperties.getBatchSize()
                    );

            log.info(
                    "Processed transaction journal reconciliation batch: runId={}, businessDate={}, lastProcessedId={}, processedCount={}, completed={}",
                    runId,
                    run.businessDate(),
                    result.lastProcessedId(),
                    result.processedCount(),
                    result.completed()
            );

            heartbeatUseCase.execute(
                    runId,
                    Instant.now()
            );

            if (result.completed()) {

                completeRunUseCase.execute(
                        runId,
                        Instant.now()
                );

                log.info(
                        "Completed transaction journal reconciliation: runId={}, businessDate={}, processedCount={}",
                        runId,
                        run.businessDate(),
                        result.processedCount()
                );

                return;
            }

            lastProcessedId =
                    result.lastProcessedId();

            processedCount =
                    result.processedCount();
        }
    }
}