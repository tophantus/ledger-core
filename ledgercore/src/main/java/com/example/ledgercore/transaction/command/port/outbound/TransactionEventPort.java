package com.example.ledgercore.transaction.command.port.outbound;

import com.example.ledgercore.transaction.event.DepositCompletedEvent;
import com.example.ledgercore.transaction.event.TransferCompletedEvent;
import com.example.ledgercore.transaction.event.WithdrawCompletedEvent;

public interface TransactionEventPort {

    void publishTransferCompleted(
            TransferCompletedEvent event
    );

    void publishDepositCompleted(
            DepositCompletedEvent event
    );

    void publishWithdrawCompleted(
            WithdrawCompletedEvent event
    );
}