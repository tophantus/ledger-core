package com.example.ledgercore.transaction.command.port.outbound;

import java.math.BigDecimal;
import java.util.UUID;

public interface AccountDepositPort {

    DepositAccountInfo getDepositInfo(
            UUID destinationAccountId
    );

    void deposit(
            UUID destinationAccountId,
            BigDecimal amount
    );

    record DepositAccountInfo(
            UUID accountId,
            String currency
    ) {
    }
}