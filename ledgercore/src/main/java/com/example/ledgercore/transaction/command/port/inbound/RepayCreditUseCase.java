package com.example.ledgercore.transaction.command.port.inbound;

import com.example.ledgercore.transaction.command.dto.RepayCreditCommand;
import com.example.ledgercore.transaction.query.dto.TransactionResponse;

public interface RepayCreditUseCase {

    TransactionResponse execute(
            RepayCreditCommand command
    );
}