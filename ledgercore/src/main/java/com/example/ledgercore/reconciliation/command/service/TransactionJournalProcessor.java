package com.example.ledgercore.reconciliation.command.service;

import com.example.ledgercore.reconciliation.command.port.inbound.CompleteReconciliationRunUseCase;
import com.example.ledgercore.reconciliation.command.port.inbound.HeartbeatReconciliationRunUseCase;
import com.example.ledgercore.reconciliation.command.port.inbound.ProcessTransactionJournalBatchUseCase;
import com.example.ledgercore.reconciliation.command.port.inbound.ProcessTransactionJournalBatchUseCase.BatchResult;
import com.example.ledgercore.reconciliation.entity.ReconciliationRun;
import com.example.ledgercore.reconciliation.enums.ReconciliationType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransactionJournalProcessor
        implements ReconciliationProcessor {

    private static final int BATCH_SIZE = 500;

    private final ProcessTransactionJournalBatchUseCase
            processBatchUseCase;

    private final HeartbeatReconciliationRunUseCase
            heartbeatUseCase;

    private final CompleteReconciliationRunUseCase
            completeRunUseCase;

    @Override
    public ReconciliationType getType() {
        return ReconciliationType.TRANSACTION_JOURNAL;
    }

    @Override
    public void process(ReconciliationRun run) {

        UUID lastProcessedId =
                run.getLastProcessedId();

        long processedCount =
                run.getProcessedCount();

        while (true) {

            BatchResult result =
                    processBatchUseCase.execute(
                            run.getId(),
                            run.getBusinessDate(),
                            lastProcessedId,
                            processedCount,
                            BATCH_SIZE
                    );

            heartbeatUseCase.execute(
                    run.getId(),
                    Instant.now()
            );

            if (result.completed()) {

                completeRunUseCase.execute(
                        run.getId(),
                        Instant.now()
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