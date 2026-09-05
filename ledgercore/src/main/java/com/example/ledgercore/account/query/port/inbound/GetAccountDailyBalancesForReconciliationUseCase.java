package com.example.ledgercore.account.query.port.inbound;

import com.example.ledgercore.account.query.dto.AccountDailyBalanceReconciliationData;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface GetAccountDailyBalancesForReconciliationUseCase {

    List<AccountDailyBalanceReconciliationData> execute(
            LocalDate businessDate,
            UUID lastProcessedId,
            int limit
    );
}