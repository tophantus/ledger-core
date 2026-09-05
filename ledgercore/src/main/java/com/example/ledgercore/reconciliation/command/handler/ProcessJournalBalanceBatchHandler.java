package com.example.ledgercore.reconciliation.command.handler;

import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.common.exception.ErrorCode;
import com.example.ledgercore.reconciliation.command.port.inbound.ProcessJournalBalanceBatchUseCase;
import com.example.ledgercore.reconciliation.command.port.outbound.JournalBalanceQueryPort;
import com.example.ledgercore.reconciliation.command.port.outbound.JournalBalanceReconciliationData;
import com.example.ledgercore.reconciliation.command.repository.ReconciliationExceptionCommandRepository;
import com.example.ledgercore.reconciliation.command.repository.ReconciliationRunCommandRepository;
import com.example.ledgercore.reconciliation.entity.ReconciliationException;
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

    private final ReconciliationRunCommandRepository
            runRepository;

    private final ReconciliationExceptionCommandRepository
            exceptionRepository;

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
                runRepository.findById(runId)
                        .orElseThrow(() ->
                                new BusinessException(
                                        ErrorCode.RECONCILIATION_RUN_NOT_FOUND
                                )
                        );

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

        return new BatchResult(
                newLastProcessedId,
                newProcessedCount,
                false
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

        exceptionRepository.save(
                ReconciliationException.builder()
                        .reconciliationRun(run)
                        .targetType(
                                ReconciliationTargetType.JOURNAL
                        )
                        .targetId(journal.id())
                        .errorCode(
                                ReconciliationErrorCode
                                        .JOURNAL_NOT_BALANCED
                        )
                        .expectedValue(
                                "debit="
                                        + journal.debitTotal()
                        )
                        .actualValue(
                                "credit="
                                        + journal.creditTotal()
                        )
                        .message(
                                "Journal entry debit and credit totals do not balance"
                        )
                        .build()
        );
    }
}