package com.example.ledgercore.transaction.command.port.inbound;

import com.example.ledgercore.transaction.command.dto.ConfirmTransferCommand;
import com.example.ledgercore.transaction.query.dto.TransactionResponse;

import java.util.UUID;

public interface ConfirmTransferUseCase {

    TransactionResponse execute(
            UUID userId,
            ConfirmTransferCommand command
    );
}