package com.example.ledgercore.transaction.config;

import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TransactionRabbitConfig {

    public static final String TRANSACTION_EXCHANGE =
            "transaction.exchange";

    public static final String ACCOUNT_BALANCE_CHANGED_ROUTING_KEY =
            "transaction.account-balance.changed";

    public static final String CREDIT_FACILITY_BALANCE_CHANGED_ROUTING_KEY =
            "transaction.credit-facility-balance.changed";

    @Bean
    public TopicExchange transactionExchange() {
        return new TopicExchange(TRANSACTION_EXCHANGE);
    }
}