package com.example.ledgercore.outbox.command.handler;

import com.example.ledgercore.outbox.command.port.inbound.ConfirmOutboxPublishUseCase;
import com.example.ledgercore.outbox.command.repository.OutboxEventCommandRepository;
import com.example.ledgercore.outbox.entity.OutboxEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ConfirmOutboxPublishHandler
        implements ConfirmOutboxPublishUseCase {

    private final OutboxEventCommandRepository outboxEventCommandRepository;

    @Override
    @Transactional
    public void execute(UUID eventId) {

        OutboxEvent event =
                outboxEventCommandRepository
                        .findById(eventId)
                        .orElse(null);

        if (event == null || event.isPublished()) {
            return;
        }

        event.markPublished();
    }
}