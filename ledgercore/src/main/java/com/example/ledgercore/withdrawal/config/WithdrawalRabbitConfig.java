package com.example.ledgercore.withdrawal.config;

import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WithdrawalRabbitConfig {

    public static final String WITHDRAWAL_EXCHANGE =
            "withdrawal.exchange";

    public static final String WITHDRAWAL_CODE_NOTIFICATION_ROUTING_KEY =
            "withdrawal.code.notification";

    @Bean
    public TopicExchange withdrawalExchange() {
        return new TopicExchange(
                WITHDRAWAL_EXCHANGE
        );
    }
}