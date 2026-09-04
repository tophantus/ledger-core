package com.example.ledgercore.businessday.config;

import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BusinessDayRabbitConfig {

    public static final String BUSINESS_DAY_EXCHANGE =
            "business-day.exchange";

    public static final String BUSINESS_DAY_CLOSED_ROUTING_KEY =
            "business-day.closed";

    @Bean
    public TopicExchange businessDayExchange() {
        return new TopicExchange(BUSINESS_DAY_EXCHANGE);
    }
}