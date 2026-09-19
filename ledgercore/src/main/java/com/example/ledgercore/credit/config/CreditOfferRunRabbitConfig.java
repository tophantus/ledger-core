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
public class CreditOfferRunRabbitConfig {

    public static final String CREDIT_OFFER_RUN_QUEUE =
            "credit.offer.run.queue";

    public static final String CREDIT_OFFER_RUN_DLX =
            "credit.offer.run.dlx";

    public static final String CREDIT_OFFER_RUN_DLQ =
            "credit.offer.run.dlq";

    public static final String CREDIT_OFFER_RUN_DLQ_ROUTING_KEY =
            "credit.offer.run.dlq";

    @Bean
    public Queue creditOfferRunQueue() {
        return QueueBuilder
                .durable(CREDIT_OFFER_RUN_QUEUE)
                .withArgument(
                        "x-dead-letter-exchange",
                        CREDIT_OFFER_RUN_DLX
                )
                .withArgument(
                        "x-dead-letter-routing-key",
                        CREDIT_OFFER_RUN_DLQ_ROUTING_KEY
                )
                .build();
    }

    @Bean
    public Binding creditOfferRunBinding(
            Queue creditOfferRunQueue,
            TopicExchange businessDayExchange
    ) {
        return BindingBuilder
                .bind(creditOfferRunQueue)
                .to(businessDayExchange)
                .with(
                        BusinessDayRabbitConfig
                                .BUSINESS_DAY_CLOSED_ROUTING_KEY
                );
    }

    @Bean
    public TopicExchange creditOfferRunDlx() {
        return new TopicExchange(CREDIT_OFFER_RUN_DLX);
    }

    @Bean
    public Queue creditOfferRunDlq() {
        return QueueBuilder
                .durable(CREDIT_OFFER_RUN_DLQ)
                .build();
    }

    @Bean
    public Binding creditOfferRunDlqBinding(
            Queue creditOfferRunDlq,
            TopicExchange creditOfferRunDlx
    ) {
        return BindingBuilder
                .bind(creditOfferRunDlq)
                .to(creditOfferRunDlx)
                .with(CREDIT_OFFER_RUN_DLQ_ROUTING_KEY);
    }
}