package com.example.ledgercore.transaction.command.port.outbound;

import com.example.ledgercore.common.currency.Currency;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public interface RepayCreditFacilityAccountBalancePort {

    void increaseBalance(
            UUID accountId,
            BigDecimal amount,
            Currency currency,
            LocalDate businessDate
    );

    void decreaseBalance(
            UUID accountId,
            BigDecimal amount,
            Currency currency,
            LocalDate businessDate
    );
}