package com.example.ledgercore.notification.mail.config;

import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.config.RetryInterceptorBuilder;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.config.StatelessRetryOperationsInterceptor;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.retry.MessageRecoverer;
import org.springframework.amqp.rabbit.retry.RepublishMessageRecoverer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WithdrawalCodeMailRabbitRetryConfig {

    @Bean
    public MessageRecoverer withdrawalCodeMailMessageRecoverer(
            RabbitTemplate rabbitTemplate,
            @Qualifier("withdrawalCodeMailDlx")
            TopicExchange withdrawalCodeMailDlx
    ) {
        return new RepublishMessageRecoverer(
                rabbitTemplate,
                withdrawalCodeMailDlx.getName(),
                WithdrawalCodeMailRabbitConfig
                        .WITHDRAWAL_CODE_MAIL_DLQ_ROUTING_KEY
        );
    }

    @Bean
    public StatelessRetryOperationsInterceptor
    withdrawalCodeMailRetryInterceptor(
            MessageRecoverer withdrawalCodeMailMessageRecoverer
    ) {
        return RetryInterceptorBuilder
                .stateless()
                .maxRetries(4)
                .backOffOptions(
                        1_000L,
                        2.0,
                        10_000L
                )
                .recoverer(withdrawalCodeMailMessageRecoverer)
                .build();
    }

    @Bean
    public SimpleRabbitListenerContainerFactory
    withdrawalCodeMailRabbitListenerContainerFactory(
            ConnectionFactory connectionFactory,
            StatelessRetryOperationsInterceptor
                    withdrawalCodeMailRetryInterceptor
    ) {
        SimpleRabbitListenerContainerFactory factory =
                new SimpleRabbitListenerContainerFactory();

        factory.setConnectionFactory(connectionFactory);
        factory.setAdviceChain(
                withdrawalCodeMailRetryInterceptor
        );
        factory.setDefaultRequeueRejected(false);

        return factory;
    }
}