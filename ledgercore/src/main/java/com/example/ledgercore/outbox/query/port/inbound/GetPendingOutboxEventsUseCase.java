package com.example.ledgercore.outbox.query.port.inbound;

import com.example.ledgercore.outbox.entity.OutboxEvent;

import java.util.List;

public interface GetPendingOutboxEventsUseCase {

    List<OutboxEvent> execute(int batchSize);
}