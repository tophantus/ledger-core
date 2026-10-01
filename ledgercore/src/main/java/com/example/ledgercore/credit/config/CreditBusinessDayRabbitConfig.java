package com.example.ledgercore.credit.config;

import com.example.ledgercore.businessday.config.BusinessDayRabbitConfig;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CreditBusinessDayRabbitConfig {

    public static final String CREDIT_BUSINESS_DAY_CLOSED_QUEUE =
            "credit.business-day.closed.queue";

    public static final String CREDIT_BUSINESS_DAY_DLX =
            "credit.business-day.closed.dlx";

    public static final String CREDIT_BUSINESS_DAY_DLQ =
            "credit.business-day.closed.dlq";

    public static final String CREDIT_BUSINESS_DAY_DLQ_ROUTING_KEY =
            "credit.business-day.closed.dlq";

    @Bean
    public Queue creditBusinessDayClosedQueue() {
        return QueueBuilder
                .durable(CREDIT_BUSINESS_DAY_CLOSED_QUEUE)
                .withArgument(
                        "x-dead-letter-exchange",
                        CREDIT_BUSINESS_DAY_DLX
                )
                .withArgument(
                        "x-dead-letter-routing-key",
                        CREDIT_BUSINESS_DAY_DLQ_ROUTING_KEY
                )
                .build();
    }

    @Bean
    public Binding creditBusinessDayClosedBinding(
            Queue creditBusinessDayClosedQueue,
            TopicExchange businessDayExchange
    ) {
        return BindingBuilder
                .bind(creditBusinessDayClosedQueue)
                .to(businessDayExchange)
                .with(
                        BusinessDayRabbitConfig
                                .BUSINESS_DAY_CLOSED_ROUTING_KEY
                );
    }

    @Bean
    public TopicExchange creditBusinessDayDlx() {
        return new TopicExchange(CREDIT_BUSINESS_DAY_DLX);
    }

    @Bean
    public Queue creditBusinessDayDlq() {
        return QueueBuilder
                .durable(CREDIT_BUSINESS_DAY_DLQ)
                .build();
    }

    @Bean
    public Binding creditBusinessDayDlqBinding(
            Queue creditBusinessDayDlq,
            TopicExchange creditBusinessDayDlx
    ) {
        return BindingBuilder
                .bind(creditBusinessDayDlq)
                .to(creditBusinessDayDlx)
                .with(CREDIT_BUSINESS_DAY_DLQ_ROUTING_KEY);
    }
}