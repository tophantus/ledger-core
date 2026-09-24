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

    public static final String WEBHOOK_ACCOUNT_BALANCE_QUEUE =
            "webhook.account-balance.queue";

    public static final String WEBHOOK_CREDIT_FACILITY_BALANCE_QUEUE =
            "webhook.credit-facility-balance.queue";

    @Bean
    public Queue webhookAccountBalanceQueue() {
        return QueueBuilder
                .durable(WEBHOOK_ACCOUNT_BALANCE_QUEUE)
                .build();
    }

    @Bean
    public Queue webhookCreditFacilityBalanceQueue() {
        return QueueBuilder
                .durable(WEBHOOK_CREDIT_FACILITY_BALANCE_QUEUE)
                .build();
    }

    @Bean
    public Binding accountBalanceChangedWebhookBinding(
            Queue webhookAccountBalanceQueue,
            TopicExchange transactionExchange
    ) {
        return BindingBuilder
                .bind(webhookAccountBalanceQueue)
                .to(transactionExchange)
                .with(
                        TransactionRabbitConfig
                                .ACCOUNT_BALANCE_CHANGED_ROUTING_KEY
                );
    }

    @Bean
    public Binding creditFacilityBalanceChangedWebhookBinding(
            Queue webhookCreditFacilityBalanceQueue,
            TopicExchange transactionExchange
    ) {
        return BindingBuilder
                .bind(webhookCreditFacilityBalanceQueue)
                .to(transactionExchange)
                .with(
                        TransactionRabbitConfig
                                .CREDIT_FACILITY_BALANCE_CHANGED_ROUTING_KEY
                );
    }
}