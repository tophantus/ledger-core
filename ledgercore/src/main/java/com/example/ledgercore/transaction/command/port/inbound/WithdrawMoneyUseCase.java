package com.example.ledgercore.transaction.command.port.inbound;

import com.example.ledgercore.transaction.command.dto.WithdrawMoneyCommand;
import com.example.ledgercore.transaction.query.dto.TransactionResponse;

public interface WithdrawMoneyUseCase {

    TransactionResponse execute(
            WithdrawMoneyCommand command
    );
}