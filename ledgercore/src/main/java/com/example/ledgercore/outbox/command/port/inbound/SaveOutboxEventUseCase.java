package com.example.ledgercore.outbox.command.port.inbound;

import java.util.UUID;

public interface SaveOutboxEventUseCase {

    UUID execute(
            String aggregateType,
            UUID aggregateId,
            String eventType,
            Object payload
    );
}