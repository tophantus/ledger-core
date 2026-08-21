package com.example.ledgercore.outbox.command.port.inbound;

import java.util.UUID;

public interface ConfirmOutboxPublishUseCase {

    void execute(UUID eventId);
}