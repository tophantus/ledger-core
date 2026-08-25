package com.example.ledgercore.outbox.adapter.outbound.rabbit;

import com.example.ledgercore.outbox.entity.OutboxEvent;
import com.example.ledgercore.outbox.event.OutboxEventType;
import org.springframework.stereotype.Component;

@Component
public class OutboxEventRabbitMapper {

    public RabbitOutboxMessage map(OutboxEvent event) {

        OutboxEventType eventType =
                OutboxEventType.fromValue(
                        event.getEventType()
                );

        validateAggregateType(event, eventType);

        return new RabbitOutboxMessage(
                eventType.getExchange(),
                eventType.getRoutingKey()
        );
    }

    private void validateAggregateType(
            OutboxEvent event,
            OutboxEventType eventType
    ) {

        String expected =
                eventType
                        .getAggregateType()
                        .getValue();

        if (!expected.equals(event.getAggregateType())) {
            throw new IllegalStateException(
                    "Outbox aggregate type mismatch. " +
                            "eventId=" + event.getId() +
                            ", expected=" + expected +
                            ", actual=" + event.getAggregateType()
            );
        }
    }
}