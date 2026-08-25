package com.example.ledgercore.webhook.adapter.inbound.rabbit;

import com.example.ledgercore.transaction.event.TransferCompletedEvent;
import com.example.ledgercore.webhook.command.port.inbound.HandleTransferCompletedWebhookUseCase;
import com.example.ledgercore.webhook.config.WebhookRabbitConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class TransferCompletedWebhookConsumer {

    private final HandleTransferCompletedWebhookUseCase
            handleTransferCompletedWebhookUseCase;

    @RabbitListener(
            queues = WebhookRabbitConfig.WEBHOOK_TRANSACTION_QUEUE
    )
    public void consume(
            TransferCompletedEvent event
    ) {
        log.debug(
                "Received transfer completed event transactionId={}",
                event.transactionId()
        );

        handleTransferCompletedWebhookUseCase.execute(
                event
        );
    }
}