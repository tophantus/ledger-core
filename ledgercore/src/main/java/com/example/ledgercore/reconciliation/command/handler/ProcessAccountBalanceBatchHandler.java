package com.example.ledgercore.reconciliation.command.handler;

import com.example.ledgercore.reconciliation.command.port.inbound.GetReconciliationRunUseCase;
import com.example.ledgercore.reconciliation.command.port.inbound.ProcessAccountBalanceBatchUseCase;
import com.example.ledgercore.reconciliation.command.port.inbound.RecordReconciliationExceptionUseCase;
import com.example.ledgercore.reconciliation.command.port.outbound.account.AccountDailyBalanceQueryPort;
import com.example.ledgercore.reconciliation.command.port.outbound.account.AccountDailyBalanceReconciliationData;
import com.example.ledgercore.reconciliation.command.port.outbound.account.AccountTransactionMovementQueryPort;
import com.example.ledgercore.reconciliation.command.port.outbound.account.AccountTransactionMovementReconciliationData;
import com.example.ledgercore.reconciliation.entity.ReconciliationRun;
import com.example.ledgercore.reconciliation.enums.ReconciliationErrorCode;
import com.example.ledgercore.reconciliation.enums.ReconciliationTargetType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProcessAccountBalanceBatchHandler
        implements ProcessAccountBalanceBatchUseCase {

    private final AccountDailyBalanceQueryPort accountDailyBalanceQueryPort;

    private final AccountTransactionMovementQueryPort
            movementQueryPort;

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

        List<AccountDailyBalanceReconciliationData> balances =
                accountDailyBalanceQueryPort.findBatch(
                        businessDate,
                        lastProcessedId,
                        batchSize
                );

        if (balances.isEmpty()) {
            return new BatchResult(
                    lastProcessedId,
                    processedCount,
                    true
            );
        }

        ReconciliationRun run =
                getReconciliationRunUseCase.execute(runId);

        List<UUID> accountIds =
                balances.stream()
                        .map(
                                AccountDailyBalanceReconciliationData
                                        ::accountId
                        )
                        .toList();

        List<AccountTransactionMovementReconciliationData> movements =
                movementQueryPort.findMovements(
                        businessDate,
                        accountIds
                );

        Map<UUID, AccountTransactionMovementReconciliationData>
                movementsByAccountId =
                movements.stream()
                        .collect(Collectors.toMap(
                                AccountTransactionMovementReconciliationData
                                        ::accountId,
                                Function.identity()
                        ));

        UUID newLastProcessedId =
                lastProcessedId;

        long newProcessedCount =
                processedCount;

        for (AccountDailyBalanceReconciliationData balance : balances) {

            AccountTransactionMovementReconciliationData movement =
                    movementsByAccountId.get(
                            balance.accountId()
                    );

            validateBalance(
                    run,
                    balance,
                    movement
            );

            newLastProcessedId =
                    balance.accountId();

            newProcessedCount++;
        }

        run.updateProgress(
                newLastProcessedId,
                newProcessedCount
        );

        boolean completed =
                balances.size() < batchSize;

        return new BatchResult(
                newLastProcessedId,
                newProcessedCount,
                completed
        );
    }

    private void validateBalance(
            ReconciliationRun run,
            AccountDailyBalanceReconciliationData balance,
            AccountTransactionMovementReconciliationData movement
    ) {

        BigDecimal totalCredit =
                movement != null
                        ? movement.totalCredit()
                        : BigDecimal.ZERO;

        BigDecimal totalDebit =
                movement != null
                        ? movement.totalDebit()
                        : BigDecimal.ZERO;

        BigDecimal expectedClosingBalance =
                balance.openingBalance()
                        .add(totalCredit)
                        .subtract(totalDebit);

        BigDecimal actualClosingBalance =
                balance.closingBalance();

        if (expectedClosingBalance
                .compareTo(actualClosingBalance) == 0) {
            return;
        }

        recordExceptionUseCase.execute(
                run.getId(),
                ReconciliationTargetType.ACCOUNT,
                balance.accountId(),
                ReconciliationErrorCode.BALANCE_MISMATCH,
                "opening="
                        + balance.openingBalance()
                        + ", credit="
                        + totalCredit
                        + ", debit="
                        + totalDebit
                        + ", closing="
                        + expectedClosingBalance,
                "closing="
                        + actualClosingBalance,
                "Account closing balance does not match "
                        + "opening balance plus net transaction movement"
        );
    }
}