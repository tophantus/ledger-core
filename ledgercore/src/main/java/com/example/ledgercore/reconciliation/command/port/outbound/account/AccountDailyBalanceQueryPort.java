package com.example.ledgercore.reconciliation.command.port.outbound.account;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface AccountDailyBalanceQueryPort {

    List<AccountDailyBalanceReconciliationData> findBatch(
            LocalDate businessDate,
            UUID lastProcessedId,
            int limit
    );
}