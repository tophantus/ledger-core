package com.example.ledgercore.transaction.command.port.inbound;

import com.example.ledgercore.transaction.command.dto.DepositMoneyCommand;
import com.example.ledgercore.transaction.query.dto.TransactionResponse;

import java.util.UUID;

public interface DepositMoneyUseCase {

    TransactionResponse execute(
            UUID adminUserId,
            DepositMoneyCommand command
    );
}