package com.example.ledgercore.outbox.command.port.inbound;

import com.example.ledgercore.outbox.entity.OutboxEvent;

public interface PublishOutboxEventUseCase {

    void execute(OutboxEvent event);
}