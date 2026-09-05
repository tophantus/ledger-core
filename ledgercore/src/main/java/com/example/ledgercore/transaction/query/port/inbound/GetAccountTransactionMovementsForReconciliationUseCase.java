package com.example.ledgercore.transaction.query.port.inbound;

import com.example.ledgercore.transaction.query.dto.AccountTransactionMovementData;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface GetAccountTransactionMovementsForReconciliationUseCase {

    List<AccountTransactionMovementData> execute(
            LocalDate businessDate,
            List<UUID> accountIds
    );
}