package com.example.ledgercore.credit.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CreditRepaymentRabbitConfig {

    public static final String CREDIT_REPAYMENT_EXCHANGE =
            "credit.repayment.exchange";

    public static final String CREDIT_REPAYMENT_ROUTING_KEY =
            "credit.repayment";

    public static final String CREDIT_REPAYMENT_QUEUE =
            "credit.repayment.queue";

    public static final String CREDIT_REPAYMENT_DLX =
            "credit.repayment.dlx";

    public static final String CREDIT_REPAYMENT_DLQ =
            "credit.repayment.dlq";

    public static final String CREDIT_REPAYMENT_DLQ_ROUTING_KEY =
            "credit.repayment.dlq";

    @Bean
    public TopicExchange creditRepaymentExchange() {
        return new TopicExchange(CREDIT_REPAYMENT_EXCHANGE);
    }

    @Bean
    public Queue creditRepaymentQueue() {
        return QueueBuilder
                .durable(CREDIT_REPAYMENT_QUEUE)
                .withArgument(
                        "x-dead-letter-exchange",
                        CREDIT_REPAYMENT_DLX
                )
                .withArgument(
                        "x-dead-letter-routing-key",
                        CREDIT_REPAYMENT_DLQ_ROUTING_KEY
                )
                .build();
    }

    @Bean
    public Binding creditRepaymentBinding(
            Queue creditRepaymentQueue,
            TopicExchange creditRepaymentExchange
    ) {
        return BindingBuilder
                .bind(creditRepaymentQueue)
                .to(creditRepaymentExchange)
                .with(CREDIT_REPAYMENT_ROUTING_KEY);
    }

    @Bean
    public TopicExchange creditRepaymentDlx() {
        return new TopicExchange(CREDIT_REPAYMENT_DLX);
    }

    @Bean
    public Queue creditRepaymentDlq() {
        return QueueBuilder
                .durable(CREDIT_REPAYMENT_DLQ)
                .build();
    }

    @Bean
    public Binding creditRepaymentDlqBinding(
            Queue creditRepaymentDlq,
            TopicExchange creditRepaymentDlx
    ) {
        return BindingBuilder
                .bind(creditRepaymentDlq)
                .to(creditRepaymentDlx)
                .with(CREDIT_REPAYMENT_DLQ_ROUTING_KEY);
    }
}