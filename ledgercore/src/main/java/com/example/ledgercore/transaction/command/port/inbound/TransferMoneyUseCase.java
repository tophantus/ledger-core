package com.example.ledgercore.transaction.command.port.inbound;

import com.example.ledgercore.transaction.command.dto.TransferMoneyCommand;
import com.example.ledgercore.transaction.query.dto.TransactionResponse;

public interface TransferMoneyUseCase {

    TransactionResponse execute(
            TransferMoneyCommand command
    );
}