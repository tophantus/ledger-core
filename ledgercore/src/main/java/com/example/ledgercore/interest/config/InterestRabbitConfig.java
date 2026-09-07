package com.example.ledgercore.interest.config;

import com.example.ledgercore.businessday.config.BusinessDayRabbitConfig;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class InterestRabbitConfig {

    public static final String INTEREST_QUEUE =
            "interest.queue";

    public static final String INTEREST_DLX =
            "interest.dlx";

    public static final String INTEREST_DLQ =
            "interest.dlq";

    public static final String INTEREST_DLQ_ROUTING_KEY =
            "interest.dlq";

    @Bean
    public Queue interestQueue() {
        return QueueBuilder
                .durable(INTEREST_QUEUE)
                .withArgument(
                        "x-dead-letter-exchange",
                        INTEREST_DLX
                )
                .withArgument(
                        "x-dead-letter-routing-key",
                        INTEREST_DLQ_ROUTING_KEY
                )
                .build();
    }

    @Bean
    public Binding interestBinding(
            Queue interestQueue,
            TopicExchange businessDayExchange
    ) {
        return BindingBuilder
                .bind(interestQueue)
                .to(businessDayExchange)
                .with(
                        BusinessDayRabbitConfig
                                .BUSINESS_DAY_CLOSED_ROUTING_KEY
                );
    }

    @Bean
    public TopicExchange interestDlx() {
        return new TopicExchange(INTEREST_DLX);
    }

    @Bean
    public Queue interestDlq() {
        return QueueBuilder
                .durable(INTEREST_DLQ)
                .build();
    }

    @Bean
    public Binding interestDlqBinding(
            Queue interestDlq,
            TopicExchange interestDlx
    ) {
        return BindingBuilder
                .bind(interestDlq)
                .to(interestDlx)
                .with(INTEREST_DLQ_ROUTING_KEY);
    }
}