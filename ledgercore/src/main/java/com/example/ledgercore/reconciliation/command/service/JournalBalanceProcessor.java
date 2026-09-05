package com.example.ledgercore.reconciliation.command.service;

import com.example.ledgercore.reconciliation.command.port.inbound.CompleteReconciliationRunUseCase;
import com.example.ledgercore.reconciliation.command.port.inbound.HeartbeatReconciliationRunUseCase;
import com.example.ledgercore.reconciliation.command.port.inbound.ProcessJournalBalanceBatchUseCase;
import com.example.ledgercore.reconciliation.command.port.inbound.ProcessJournalBalanceBatchUseCase.BatchResult;
import com.example.ledgercore.reconciliation.entity.ReconciliationRun;
import com.example.ledgercore.reconciliation.enums.ReconciliationType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class JournalBalanceProcessor
        implements ReconciliationProcessor {

    private static final int BATCH_SIZE = 500;

    private final ProcessJournalBalanceBatchUseCase
            processBatchUseCase;

    private final HeartbeatReconciliationRunUseCase
            heartbeatUseCase;

    private final CompleteReconciliationRunUseCase
            completeRunUseCase;

    @Override
    public ReconciliationType getType() {
        return ReconciliationType.JOURNAL_BALANCE;
    }

    @Override
    public void process(ReconciliationRun run) {

        UUID runId = run.getId();
        UUID lastProcessedId = run.getLastProcessedId();
        long processedCount = run.getProcessedCount();

        log.info(
                "Starting journal balance reconciliation: runId={}, businessDate={}, lastProcessedId={}, processedCount={}",
                runId,
                run.getBusinessDate(),
                lastProcessedId,
                processedCount
        );

        while (true) {

            BatchResult result =
                    processBatchUseCase.execute(
                            runId,
                            run.getBusinessDate(),
                            lastProcessedId,
                            processedCount,
                            BATCH_SIZE
                    );

            log.info(
                    "Processed journal balance reconciliation batch: runId={}, businessDate={}, lastProcessedId={}, processedCount={}, completed={}",
                    runId,
                    run.getBusinessDate(),
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
                        "Completed journal balance reconciliation: runId={}, businessDate={}, processedCount={}",
                        runId,
                        run.getBusinessDate(),
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