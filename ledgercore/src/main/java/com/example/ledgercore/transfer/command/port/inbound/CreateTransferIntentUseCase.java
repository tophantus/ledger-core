package com.example.ledgercore.transfer.command.port.inbound;

import com.example.ledgercore.transfer.command.dto.CreateTransferIntentCommand;
import com.example.ledgercore.transfer.command.dto.CreateTransferIntentResult;

import java.util.UUID;

public interface CreateTransferIntentUseCase {

    CreateTransferIntentResult execute(
            UUID userId,
            CreateTransferIntentCommand command
    );
}