package com.example.ledgercore.transaction.command.port.inbound;

import com.example.ledgercore.transaction.command.dto.WithdrawMoneyCommand;
import com.example.ledgercore.transaction.query.dto.TransactionResponse;

import java.util.UUID;

public interface WithdrawMoneyUseCase {

    TransactionResponse execute(
            UUID userId,
            WithdrawMoneyCommand command
    );
}