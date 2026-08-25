package com.example.ledgercore.outbox.command.port.inbound;

public interface PublishPendingOutboxEventsUseCase {

    int execute(int batchSize);
}