package com.example.ledgercore.outbox.service;

import com.example.ledgercore.outbox.entity.OutboxEvent;
import com.example.ledgercore.outbox.repository.OutboxEventRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OutboxService {

    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    public UUID save(
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

        return outboxEventRepository
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