package com.example.ledgercore.outbox.command.handler;

import com.example.ledgercore.outbox.command.port.inbound.SaveOutboxEventUseCase;
import com.example.ledgercore.outbox.command.repository.OutboxEventCommandRepository;
import com.example.ledgercore.outbox.entity.OutboxEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SaveOutboxEventHandler
        implements SaveOutboxEventUseCase {

    private final OutboxEventCommandRepository outboxEventCommandRepository;
    private final ObjectMapper objectMapper;

    @Override
    public UUID execute(
            String aggregateType,
            UUID aggregateId,
            String eventType,
            Object payload
    ) {
        OutboxEvent event = OutboxEvent.builder()
                .aggregateType(aggregateType)
                .aggregateId(aggregateId)
                .eventType(eventType)
                .payload(serialize(payload))
                .build();

        return outboxEventCommandRepository
                .save(event)
                .getId();
    }

    private String serialize(Object payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(
                    "Failed to serialize outbox event payload",
                    e
            );
        }
    }
}