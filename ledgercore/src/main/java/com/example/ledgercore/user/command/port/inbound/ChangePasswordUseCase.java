package com.example.ledgercore.user.command.port.inbound;

import java.util.UUID;

public interface ChangePasswordUseCase {

    void execute(
            UUID userId,
            String passwordHash
    );
}