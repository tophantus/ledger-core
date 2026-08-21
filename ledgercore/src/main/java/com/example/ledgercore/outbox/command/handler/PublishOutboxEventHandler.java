package com.example.ledgercore.outbox.command.handler;

import com.example.ledgercore.outbox.command.port.inbound.PublishOutboxEventUseCase;
import com.example.ledgercore.outbox.command.port.outbound.OutboxMessagePublisherPort;
import com.example.ledgercore.outbox.entity.OutboxEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PublishOutboxEventHandler
        implements PublishOutboxEventUseCase {

    private final OutboxMessagePublisherPort
            outboxMessagePublisherPort;

    @Override
    @Transactional(readOnly = true)
    public void execute(OutboxEvent event) {
        if (event.isPublished()) {
            return;
        }

        outboxMessagePublisherPort.publish(event);
    }
}