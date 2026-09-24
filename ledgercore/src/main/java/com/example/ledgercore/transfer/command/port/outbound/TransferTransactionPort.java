package com.example.ledgercore.transfer.command.port.outbound;

import com.example.ledgercore.transaction.command.dto.TransferMoneyCommand;
import com.example.ledgercore.transaction.query.dto.TransactionResponse;

public interface TransferTransactionPort {

    TransactionResponse transfer(
            TransferMoneyCommand command
    );
}