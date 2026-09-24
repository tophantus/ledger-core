package com.example.ledgercore.webhook.adapter.inbound.rabbit;

import com.example.ledgercore.transaction.event.AccountBalanceChangedEvent;
import com.example.ledgercore.webhook.command.port.inbound.HandleAccountBalanceChangedWebhookUseCase;
import com.example.ledgercore.webhook.config.WebhookRabbitConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AccountBalanceChangeWebhookConsumer {

    private final HandleAccountBalanceChangedWebhookUseCase
            handleAccountBalanceChangedWebhookUseCase;

    @RabbitListener(
            queues = WebhookRabbitConfig.WEBHOOK_ACCOUNT_BALANCE_QUEUE
    )
    public void consume(
            AccountBalanceChangedEvent event
    ) {
        log.debug(
                "Received account balance changed event transactionId={}",
                event.transactionId()
        );

        handleAccountBalanceChangedWebhookUseCase.execute(
                event
        );
    }
}