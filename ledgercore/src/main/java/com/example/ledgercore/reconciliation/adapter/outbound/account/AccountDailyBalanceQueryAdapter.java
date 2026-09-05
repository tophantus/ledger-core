package com.example.ledgercore.reconciliation.adapter.outbound.account;

import com.example.ledgercore.account.query.port.inbound.GetAccountDailyBalancesForReconciliationUseCase;
import com.example.ledgercore.reconciliation.command.port.outbound.account.AccountDailyBalanceQueryPort;
import com.example.ledgercore.reconciliation.command.port.outbound.account.AccountDailyBalanceReconciliationData;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AccountDailyBalanceQueryAdapter
        implements AccountDailyBalanceQueryPort {

    private final GetAccountDailyBalancesForReconciliationUseCase useCase;

    @Override
    public List<AccountDailyBalanceReconciliationData> findBatch(
            LocalDate businessDate,
            UUID lastProcessedId,
            int limit
    ) {

        return useCase.execute(
                        businessDate,
                        lastProcessedId,
                        limit
                )
                .stream()
                .map(data ->
                        new AccountDailyBalanceReconciliationData(
                                data.accountId(),
                                data.businessDate(),
                                data.openingBalance(),
                                data.closingBalance()
                        )
                )
                .toList();
    }
}