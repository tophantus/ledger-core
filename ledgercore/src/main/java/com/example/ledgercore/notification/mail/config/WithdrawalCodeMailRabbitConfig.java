package com.example.ledgercore.notification.mail.config;

import com.example.ledgercore.withdrawal.config.WithdrawalRabbitConfig;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WithdrawalCodeMailRabbitConfig {

    public static final String WITHDRAWAL_CODE_MAIL_QUEUE =
            "notification.mail.withdrawal-code.queue";

    public static final String WITHDRAWAL_CODE_MAIL_DLX =
            "notification.mail.withdrawal-code.dlx";

    public static final String WITHDRAWAL_CODE_MAIL_DLQ =
            "notification.mail.withdrawal-code.dlq";

    public static final String WITHDRAWAL_CODE_MAIL_DLQ_ROUTING_KEY =
            "notification.mail.withdrawal-code.dlq";

    @Bean
    public Queue withdrawalCodeMailQueue() {
        return QueueBuilder
                .durable(WITHDRAWAL_CODE_MAIL_QUEUE)
                .withArgument(
                        "x-dead-letter-exchange",
                        WITHDRAWAL_CODE_MAIL_DLX
                )
                .withArgument(
                        "x-dead-letter-routing-key",
                        WITHDRAWAL_CODE_MAIL_DLQ_ROUTING_KEY
                )
                .build();
    }

    @Bean
    public Binding withdrawalCodeMailBinding(
            Queue withdrawalCodeMailQueue,
            @Qualifier("withdrawalExchange")
            TopicExchange withdrawalExchange
    ) {
        return BindingBuilder
                .bind(withdrawalCodeMailQueue)
                .to(withdrawalExchange)
                .with(
                        WithdrawalRabbitConfig
                                .WITHDRAWAL_CODE_NOTIFICATION_ROUTING_KEY
                );
    }

    @Bean
    public TopicExchange withdrawalCodeMailDlx() {
        return new TopicExchange(
                WITHDRAWAL_CODE_MAIL_DLX
        );
    }

    @Bean
    public Queue withdrawalCodeMailDlq() {
        return QueueBuilder
                .durable(WITHDRAWAL_CODE_MAIL_DLQ)
                .build();
    }

    @Bean
    public Binding withdrawalCodeMailDlqBinding(
            Queue withdrawalCodeMailDlq,
            TopicExchange withdrawalCodeMailDlx
    ) {
        return BindingBuilder
                .bind(withdrawalCodeMailDlq)
                .to(withdrawalCodeMailDlx)
                .with(
                        WITHDRAWAL_CODE_MAIL_DLQ_ROUTING_KEY
                );
    }
}