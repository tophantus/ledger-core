package com.example.ledgercore.reconciliation.command.port.outbound.account;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface AccountTransactionMovementQueryPort {

    List<AccountTransactionMovementReconciliationData> findMovements(
            LocalDate businessDate,
            List<UUID> accountIds
    );
}