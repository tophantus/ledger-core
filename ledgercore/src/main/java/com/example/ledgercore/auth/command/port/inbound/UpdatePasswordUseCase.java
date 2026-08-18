package com.example.ledgercore.auth.command.port.inbound;

import com.example.ledgercore.auth.command.dto.UpdatePasswordCommand;

import java.util.UUID;

public interface UpdatePasswordUseCase {

    void execute(
            UUID userId,
            UpdatePasswordCommand command
    );
}