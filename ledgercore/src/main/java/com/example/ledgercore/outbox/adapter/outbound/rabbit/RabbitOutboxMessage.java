package com.example.ledgercore.outbox.adapter.outbound.rabbit;

public record RabbitOutboxMessage(
        String exchange,
        String routingKey
) {
}