package com.example.ledgercore.transaction.command.port.outbound;

import com.example.ledgercore.common.currency.Currency;

import java.math.BigDecimal;
import java.time.LocalDate;
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
            BigDecimal amount,
            LocalDate businessDate
    );

    record WithdrawAccountInfo(
            UUID accountId,
            UUID userId,
            Currency currency,
            BigDecimal availableBalance
    ) {
    }
}