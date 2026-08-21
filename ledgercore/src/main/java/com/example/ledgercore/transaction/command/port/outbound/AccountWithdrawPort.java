package com.example.ledgercore.transaction.command.port.outbound;

import java.math.BigDecimal;
import java.util.UUID;

public interface AccountWithdrawPort {

    WithdrawAccountInfo getWithdrawInfo(
            UUID userId,
            UUID sourceAccountId
    );

    void verifySourceAccountAccess(
            UUID userId,
            UUID sourceAccountId
    );

    void withdraw(
            UUID sourceAccountId,
            BigDecimal amount
    );

    record WithdrawAccountInfo(
            UUID accountId,
            String currency,
            BigDecimal balance
    ) {
    }
}