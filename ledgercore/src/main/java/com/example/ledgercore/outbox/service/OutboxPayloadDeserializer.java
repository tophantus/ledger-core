package com.example.ledgercore.outbox.service;

import com.example.ledgercore.outbox.entity.OutboxEvent;
import com.example.ledgercore.outbox.event.OutboxEventType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
public class OutboxPayloadDeserializer {

    private final ObjectMapper objectMapper;

    public Object deserialize(OutboxEvent event) {

        try {
            OutboxEventType eventType =
                    OutboxEventType.fromValue(
                            event.getEventType()
                    );

            return objectMapper.readValue(
                    event.getPayload(),
                    eventType.getPayloadType()
            );

        } catch (Exception e) {
            throw new IllegalStateException(
                    "Failed to deserialize outbox event: " +
                            "id=" + event.getId() +
                            ", eventType=" + event.getEventType(),
                    e
            );
        }
    }
}