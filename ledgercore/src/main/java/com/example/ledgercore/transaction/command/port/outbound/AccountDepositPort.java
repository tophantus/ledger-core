package com.example.ledgercore.transaction.command.port.outbound;

import com.example.ledgercore.common.currency.Currency;

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
            Currency currency
    ) {
    }
}