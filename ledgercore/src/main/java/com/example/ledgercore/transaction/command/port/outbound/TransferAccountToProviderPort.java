package com.example.ledgercore.transaction.command.port.outbound;

import com.example.ledgercore.common.currency.Currency;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public interface TransferAccountToProviderPort {

    void decreaseSourceAccount(
            UUID accountId,
            BigDecimal amount,
            Currency currency,
            LocalDate businessDate
    );

    void increaseProviderAccount(
            UUID providerAccountId,
            BigDecimal amount,
            Currency currency,
            LocalDate businessDate
    );
}