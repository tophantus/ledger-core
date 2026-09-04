package com.example.ledgercore.transaction.command.port.outbound;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public interface AccountDepositPort {

    DepositAccountInfo getDepositInfo(
            UUID destinationAccountId
    );

    void deposit(
            UUID destinationAccountId,
            BigDecimal amount,
            LocalDate businessDate
    );

    record DepositAccountInfo(
            UUID accountId,
            String currency
    ) {
    }
}