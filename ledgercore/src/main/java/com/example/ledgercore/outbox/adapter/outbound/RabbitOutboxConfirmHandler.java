package com.example.ledgercore.outbox.adapter.outbound;

import com.example.ledgercore.outbox.command.port.inbound.ConfirmOutboxPublishUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class RabbitOutboxConfirmHandler {

    private final ConfirmOutboxPublishUseCase
            confirmOutboxPublishUseCase;

    public void handle(
            CorrelationData correlationData,
            boolean ack,
            String cause
    ) {
        if (correlationData == null) {
            log.error(
                    "RabbitMQ confirm received without correlation data"
            );
            return;
        }

        UUID eventId = UUID.fromString(
                correlationData.getId()
        );

        if (!ack) {
            log.error(
                    "Outbox event publish failed eventId={} cause={}",
                    eventId,
                    cause
            );
            return;
        }

        log.debug(
                "Outbox event published successfully eventId={}",
                eventId
        );

        confirmOutboxPublishUseCase.execute(eventId);
    }
}