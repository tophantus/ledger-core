package com.example.ledgercore.webhook.config;

import com.example.ledgercore.transaction.config.TransactionRabbitConfig;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WebhookRabbitConfig {

    public static final String WEBHOOK_TRANSACTION_QUEUE =
            "webhook.transaction.queue";

    @Bean
    public Queue webhookTransactionQueue() {
        return QueueBuilder
                .durable(WEBHOOK_TRANSACTION_QUEUE)
                .build();
    }

    @Bean
    public Binding transferCompletedWebhookBinding(
            Queue webhookTransactionQueue,
            TopicExchange transactionExchange
    ) {
        return BindingBuilder
                .bind(webhookTransactionQueue)
                .to(transactionExchange)
                .with(
                        TransactionRabbitConfig
                                .TRANSFER_COMPLETED_ROUTING_KEY
                );
    }
}