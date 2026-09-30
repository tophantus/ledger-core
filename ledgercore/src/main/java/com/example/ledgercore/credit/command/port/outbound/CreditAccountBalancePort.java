package com.example.ledgercore.credit.command.port.outbound;

import com.example.ledgercore.common.currency.Currency;

import java.math.BigDecimal;
import java.util.UUID;

public interface CreditAccountBalancePort {

    AccountBalance getActiveAccount(
            UUID accountId
    );

    record AccountBalance(
            UUID accountId,
            BigDecimal availableBalance,
            Currency currency
    ) {
    }
}