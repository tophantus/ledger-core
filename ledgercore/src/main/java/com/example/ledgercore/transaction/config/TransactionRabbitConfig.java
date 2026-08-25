package com.example.ledgercore.transaction.config;

import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TransactionRabbitConfig {

    public static final String TRANSACTION_EXCHANGE =
            "transaction.exchange";

    public static final String TRANSFER_COMPLETED_ROUTING_KEY =
            "transaction.transfer.completed";

    public static final String DEPOSIT_COMPLETED_ROUTING_KEY =
            "transaction.deposit.completed";

    public static final String WITHDRAW_COMPLETED_ROUTING_KEY =
            "transaction.withdraw.completed";

    @Bean
    public TopicExchange transactionExchange() {
        return new TopicExchange(TRANSACTION_EXCHANGE);
    }
}