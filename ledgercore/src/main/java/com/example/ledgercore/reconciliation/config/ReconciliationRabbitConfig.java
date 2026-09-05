package com.example.ledgercore.reconciliation.config;

import com.example.ledgercore.businessday.config.BusinessDayRabbitConfig;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ReconciliationRabbitConfig {

    public static final String RECONCILIATION_QUEUE =
            "reconciliation.queue";

    public static final String RECONCILIATION_DLX =
            "reconciliation.dlx";

    public static final String RECONCILIATION_DLQ =
            "reconciliation.dlq";

    public static final String RECONCILIATION_DLQ_ROUTING_KEY =
            "reconciliation.dlq";

    @Bean
    public Queue reconciliationQueue() {
        return QueueBuilder
                .durable(RECONCILIATION_QUEUE)
                .withArgument(
                        "x-dead-letter-exchange",
                        RECONCILIATION_DLX
                )
                .withArgument(
                        "x-dead-letter-routing-key",
                        RECONCILIATION_DLQ_ROUTING_KEY
                )
                .build();
    }

    @Bean
    public Binding reconciliationBinding(
            Queue reconciliationQueue,
            TopicExchange businessDayExchange
    ) {
        return BindingBuilder
                .bind(reconciliationQueue)
                .to(businessDayExchange)
                .with(
                        BusinessDayRabbitConfig
                                .BUSINESS_DAY_CLOSED_ROUTING_KEY
                );
    }

    @Bean
    public TopicExchange reconciliationDlx() {
        return new TopicExchange(RECONCILIATION_DLX);
    }

    @Bean
    public Queue reconciliationDlq() {
        return QueueBuilder
                .durable(RECONCILIATION_DLQ)
                .build();
    }

    @Bean
    public Binding reconciliationDlqBinding(
            Queue reconciliationDlq,
            TopicExchange reconciliationDlx
    ) {
        return BindingBuilder
                .bind(reconciliationDlq)
                .to(reconciliationDlx)
                .with(RECONCILIATION_DLQ_ROUTING_KEY);
    }
}