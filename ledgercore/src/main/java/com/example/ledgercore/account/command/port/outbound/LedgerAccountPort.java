package com.example.ledgercore.account.command.port.outbound;

import com.example.ledgercore.common.currency.Currency;

import java.util.UUID;

public interface LedgerAccountPort {

    UUID createCustomerAccount(
            String accountCode,
            Currency currency
    );
}