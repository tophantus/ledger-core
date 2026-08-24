package com.example.ledgercore.outbox.adapter.outbound;

import com.example.ledgercore.outbox.adapter.outbound.rabbit.OutboxEventRabbitMapper;
import com.example.ledgercore.outbox.adapter.outbound.rabbit.RabbitOutboxMessage;
import com.example.ledgercore.outbox.command.port.outbound.OutboxMessagePublisherPort;
import com.example.ledgercore.outbox.entity.OutboxEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RabbitOutboxPublisherAdapter
        implements OutboxMessagePublisherPort {

    private final RabbitTemplate rabbitTemplate;
    private final OutboxEventRabbitMapper mapper;

    @Override
    public void publish(OutboxEvent event) {

        RabbitOutboxMessage message =
                mapper.map(event);

        CorrelationData correlationData =
                new CorrelationData(
                        event.getId().toString()
                );

        rabbitTemplate.convertAndSend(
                message.exchange(),
                message.routingKey(),
                event.getPayload(),
                correlationData
        );
    }
}