package com.example.ledgercore.outbox.adapter.outbound;

import com.example.ledgercore.outbox.command.port.outbound.OutboxMessagePublisherPort;
import com.example.ledgercore.outbox.entity.OutboxEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RabbitOutboxPublisherAdapter
        implements OutboxMessagePublisherPort {

    private final RabbitTemplate rabbitTemplate;

    @Override
    public void publish(OutboxEvent event) {

        CorrelationData correlationData =
                new CorrelationData(
                        event.getId().toString()
                );

        rabbitTemplate.convertAndSend(
                event.getAggregateType(),
                event.getEventType(),
                event.getPayload(),
                correlationData
        );
    }
}