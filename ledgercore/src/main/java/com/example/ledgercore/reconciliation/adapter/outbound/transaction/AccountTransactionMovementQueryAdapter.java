package com.example.ledgercore.reconciliation.adapter.outbound.transaction;

import com.example.ledgercore.reconciliation.command.port.outbound.account.AccountTransactionMovementReconciliationData;
import com.example.ledgercore.reconciliation.command.port.outbound.account.AccountTransactionMovementQueryPort;
import com.example.ledgercore.transaction.query.port.inbound.GetAccountTransactionMovementsForReconciliationUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AccountTransactionMovementQueryAdapter
        implements AccountTransactionMovementQueryPort {

    private final GetAccountTransactionMovementsForReconciliationUseCase useCase;

    @Override
    public List<AccountTransactionMovementReconciliationData> findMovements(
            LocalDate businessDate,
            List<UUID> accountIds
    ) {

        return useCase.execute(
                        businessDate,
                        accountIds
                )
                .stream()
                .map(data ->
                        new AccountTransactionMovementReconciliationData(
                                data.accountId(),
                                data.totalCredit(),
                                data.totalDebit()
                        )
                )
                .toList();
    }
}