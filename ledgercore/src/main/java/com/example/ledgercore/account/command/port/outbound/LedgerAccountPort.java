package com.example.ledgercore.account.command.port.outbound;

import java.util.UUID;

public interface LedgerAccountPort {

    UUID createCustomerAccount(
            String accountCode,
            String currency
    );
}