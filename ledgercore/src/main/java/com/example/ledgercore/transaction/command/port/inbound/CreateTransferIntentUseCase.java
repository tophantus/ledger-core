package com.example.ledgercore.transaction.command.port.inbound;

import com.example.ledgercore.transaction.command.dto.CreateTransferIntentCommand;
import com.example.ledgercore.transaction.command.dto.CreateTransferIntentResult;

import java.util.UUID;

public interface CreateTransferIntentUseCase {

    CreateTransferIntentResult execute(
            UUID userId,
            CreateTransferIntentCommand command
    );
}