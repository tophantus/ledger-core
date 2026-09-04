package com.example.ledgercore.reconciliation.command.handler;

import com.example.ledgercore.reconciliation.command.port.inbound.ProcessTransactionJournalBatchUseCase;
import com.example.ledgercore.reconciliation.command.port.outbound.JournalQueryPort;
import com.example.ledgercore.reconciliation.command.port.outbound.JournalReconciliationData;
import com.example.ledgercore.reconciliation.command.port.outbound.TransactionQueryPort;
import com.example.ledgercore.reconciliation.command.port.outbound.TransactionReconciliationData;
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

        ReconciliationRun run =
                runRepository.findById(runId)
                        .orElseThrow();

        UUID newLastProcessedId =
                lastProcessedId;

        long newProcessedCount =
                processedCount;

        for (TransactionReconciliationData transaction :
                transactions) {

            JournalReconciliationData journal =
                    journalsByTransactionId.get(
                            transaction.id()
                    );

            if (journal == null) {

                exceptionRepository.save(
                        ReconciliationException.builder()
                                .reconciliationRun(run)
                                .targetType(
                                        ReconciliationTargetType.TRANSACTION
                                )
                                .targetId(transaction.id())
                                .errorCode(
                                        ReconciliationErrorCode
                                                .JOURNAL_NOT_FOUND
                                )
                                .expectedValue(
                                        "JournalEntry exists"
                                )
                                .actualValue(
                                        "null"
                                )
                                .message(
                                        "Transaction has no corresponding journal entry"
                                )
                                .build()
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

            newLastProcessedId =
                    transaction.id();

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

    private void validateBusinessDate(
            ReconciliationRun run,
            TransactionReconciliationData transaction,
            JournalReconciliationData journal
    ) {

        if (!transaction.businessDate()
                .equals(journal.businessDate())) {

            exceptionRepository.save(
                    ReconciliationException.builder()
                            .reconciliationRun(run)
                            .targetType(
                                    ReconciliationTargetType.TRANSACTION
                            )
                            .targetId(transaction.id())
                            .errorCode(
                                    ReconciliationErrorCode
                                            .BUSINESS_DATE_MISMATCH
                            )
                            .expectedValue(
                                    transaction.businessDate()
                                            .toString()
                            )
                            .actualValue(
                                    journal.businessDate()
                                            .toString()
                            )
                            .message(
                                    "Transaction and journal business dates do not match"
                            )
                            .build()
            );
        }
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

        if (debitMismatch || creditMismatch) {

            exceptionRepository.save(
                    ReconciliationException.builder()
                            .reconciliationRun(run)
                            .targetType(
                                    ReconciliationTargetType.TRANSACTION
                            )
                            .targetId(transaction.id())
                            .errorCode(
                                    ReconciliationErrorCode
                                            .TRANSACTION_AMOUNT_MISMATCH
                            )
                            .expectedValue(
                                    "amount="
                                            + transaction.amount()
                            )
                            .actualValue(
                                    "debit="
                                            + journal.debitTotal()
                                            + ", credit="
                                            + journal.creditTotal()
                            )
                            .message(
                                    "Transaction amount does not match journal debit and credit totals"
                            )
                            .build()
            );
        }
    }
}