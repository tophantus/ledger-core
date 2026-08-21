package com.example.ledgercore.outbox.command.port.outbound;

import com.example.ledgercore.outbox.entity.OutboxEvent;

public interface OutboxMessagePublisherPort {

    void publish(OutboxEvent event);
}