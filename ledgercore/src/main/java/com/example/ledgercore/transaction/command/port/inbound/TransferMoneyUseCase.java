package com.example.ledgercore.transaction.command.port.inbound;

import com.example.ledgercore.transaction.command.dto.TransferMoneyCommand;
import com.example.ledgercore.transaction.query.dto.TransactionResponse;

import java.util.UUID;

public interface TransferMoneyUseCase {

    TransactionResponse execute(
            UUID userId,
            TransferMoneyCommand command
    );
}