package com.example.ledgercore.reconciliation.command.handler;

import com.example.ledgercore.reconciliation.command.port.inbound.GetReconciliationRunUseCase;
import com.example.ledgercore.reconciliation.command.port.inbound.ProcessTransactionJournalBatchUseCase;
import com.example.ledgercore.reconciliation.command.port.inbound.RecordReconciliationExceptionUseCase;
import com.example.ledgercore.reconciliation.command.port.outbound.ledger.JournalQueryPort;
import com.example.ledgercore.reconciliation.command.port.outbound.ledger.JournalReconciliationData;
import com.example.ledgercore.reconciliation.command.port.outbound.transaction.TransactionQueryPort;
import com.example.ledgercore.reconciliation.command.port.outbound.transaction.TransactionReconciliationData;
import com.example.ledgercore.reconciliation.entity.ReconciliationRun;
import com.example.ledgercore.reconciliation.enums.ReconciliationErrorCode;
import com.example.ledgercore.reconciliation.enums.ReconciliationTargetType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProcessTransactionJournalBatchHandler
        implements ProcessTransactionJournalBatchUseCase {

    private final TransactionQueryPort transactionQueryPort;
    private final JournalQueryPort journalQueryPort;

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

        List<TransactionReconciliationData> transactions =
                transactionQueryPort.findBatch(
                        businessDate,
                        lastProcessedId,
                        batchSize
                );

        if (transactions.isEmpty()) {
            return new BatchResult(
                    lastProcessedId,
                    processedCount,
                    true
            );
        }

        ReconciliationRun run =
                getReconciliationRunUseCase.execute(runId);

        List<UUID> transactionIds =
                transactions.stream()
                        .map(TransactionReconciliationData::id)
                        .toList();

        List<JournalReconciliationData> journals =
                journalQueryPort.findByTransactionIds(
                        transactionIds
                );

        Map<UUID, JournalReconciliationData>
                journalsByTransactionId =
                new HashMap<>();

        for (JournalReconciliationData journal : journals) {
            journalsByTransactionId.put(
                    journal.transactionId(),
                    journal
            );
        }

        UUID newLastProcessedId = lastProcessedId;
        long newProcessedCount = processedCount;

        for (TransactionReconciliationData transaction :
                transactions) {

            JournalReconciliationData journal =
                    journalsByTransactionId.get(
                            transaction.id()
                    );

            if (journal == null) {

                recordExceptionUseCase.execute(
                        run.getId(),
                        ReconciliationTargetType.TRANSACTION,
                        transaction.id(),
                        ReconciliationErrorCode.JOURNAL_NOT_FOUND,
                        "JournalEntry exists",
                        "null",
                        "Transaction has no corresponding journal entry"
                );

            } else {

                validateBusinessDate(
                        run,
                        transaction,
                        journal
                );

                validateAmount(
                        run,
                        transaction,
                        journal
                );
            }

            newLastProcessedId = transaction.id();
            newProcessedCount++;
        }

        run.updateProgress(
                newLastProcessedId,
                newProcessedCount
        );

        boolean completed =
                transactions.size() < batchSize;

        return new BatchResult(
                newLastProcessedId,
                newProcessedCount,
                completed
        );
    }

    private void validateBusinessDate(
            ReconciliationRun run,
            TransactionReconciliationData transaction,
            JournalReconciliationData journal
    ) {

        if (transaction.businessDate()
                .equals(journal.businessDate())) {
            return;
        }

        recordExceptionUseCase.execute(
                run.getId(),
                ReconciliationTargetType.TRANSACTION,
                transaction.id(),
                ReconciliationErrorCode.BUSINESS_DATE_MISMATCH,
                transaction.businessDate().toString(),
                journal.businessDate().toString(),
                "Transaction and journal business dates do not match"
        );
    }

    private void validateAmount(
            ReconciliationRun run,
            TransactionReconciliationData transaction,
            JournalReconciliationData journal
    ) {

        boolean debitMismatch =
                transaction.amount()
                        .compareTo(journal.debitTotal()) != 0;

        boolean creditMismatch =
                transaction.amount()
                        .compareTo(journal.creditTotal()) != 0;

        if (!debitMismatch && !creditMismatch) {
            return;
        }

        recordExceptionUseCase.execute(
                run.getId(),
                ReconciliationTargetType.TRANSACTION,
                transaction.id(),
                ReconciliationErrorCode.TRANSACTION_AMOUNT_MISMATCH,
                "amount=" + transaction.amount(),
                "debit=" + journal.debitTotal()
                        + ", credit=" + journal.creditTotal(),
                "Transaction amount does not match "
                        + "journal debit and credit totals"
        );
    }
}