package com.example.ledgercore.reconciliation.command.handler;

import com.example.ledgercore.reconciliation.command.port.inbound.GetReconciliationRunUseCase;
import com.example.ledgercore.reconciliation.command.port.inbound.ProcessJournalBalanceBatchUseCase;
import com.example.ledgercore.reconciliation.command.port.inbound.RecordReconciliationExceptionUseCase;
import com.example.ledgercore.reconciliation.command.port.outbound.ledger.JournalBalanceQueryPort;
import com.example.ledgercore.reconciliation.command.port.outbound.ledger.JournalBalanceReconciliationData;
import com.example.ledgercore.reconciliation.entity.ReconciliationRun;
import com.example.ledgercore.reconciliation.enums.ReconciliationErrorCode;
import com.example.ledgercore.reconciliation.enums.ReconciliationTargetType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProcessJournalBalanceBatchHandler
        implements ProcessJournalBalanceBatchUseCase {

    private final JournalBalanceQueryPort journalBalanceQueryPort;

    private final GetReconciliationRunUseCase
            getReconciliationRunUseCase;

    private final RecordReconciliationExceptionUseCase
            recordExceptionUseCase;

    @Override
    @Transactional
    public BatchResult execute(
            UUID runId,
            LocalDate businessDate,
            UUID lastProcessedId,
            long processedCount,
            int batchSize
    ) {

        List<JournalBalanceReconciliationData> journals =
                journalBalanceQueryPort.findBatch(
                        businessDate,
                        lastProcessedId,
                        batchSize
                );

        if (journals.isEmpty()) {
            return new BatchResult(
                    lastProcessedId,
                    processedCount,
                    true
            );
        }

        ReconciliationRun run =
                getReconciliationRunUseCase.execute(runId);

        UUID newLastProcessedId = lastProcessedId;
        long newProcessedCount = processedCount;

        for (JournalBalanceReconciliationData journal : journals) {

            validateBalance(run, journal);

            newLastProcessedId = journal.id();
            newProcessedCount++;
        }

        run.updateProgress(
                newLastProcessedId,
                newProcessedCount
        );

        boolean completed =
                journals.size() < batchSize;

        return new BatchResult(
                newLastProcessedId,
                newProcessedCount,
                completed
        );
    }

    private void validateBalance(
            ReconciliationRun run,
            JournalBalanceReconciliationData journal
    ) {

        if (journal.debitTotal()
                .compareTo(journal.creditTotal()) == 0) {
            return;
        }

        recordExceptionUseCase.execute(
                run.getId(),
                ReconciliationTargetType.JOURNAL,
                journal.id(),
                ReconciliationErrorCode.JOURNAL_NOT_BALANCED,
                "debit=" + journal.debitTotal(),
                "credit=" + journal.creditTotal(),
                "Journal entry debit and credit totals do not balance"
        );
    }
}