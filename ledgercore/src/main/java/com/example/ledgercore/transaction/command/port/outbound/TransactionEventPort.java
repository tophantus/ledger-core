package com.example.ledgercore.transaction.command.port.outbound;

import com.example.ledgercore.transaction.event.*;

public interface TransactionEventPort {

    void publishAccountBalanceChanged(
            AccountBalanceChangedEvent event
    );

    void publishCreditFacilityBalanceChanged(
            CreditFacilityBalanceChangedEvent event
    );
}