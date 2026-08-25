package com.example.ledgercore.notification.mail.config;

import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.config.RetryInterceptorBuilder;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.config.StatelessRetryOperationsInterceptor;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.retry.MessageRecoverer;
import org.springframework.amqp.rabbit.retry.RepublishMessageRecoverer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MailRabbitRetryConfig {

    @Bean
    public MessageRecoverer mailMessageRecoverer(
            RabbitTemplate rabbitTemplate,
            TopicExchange mailDlx
    ) {
        return new RepublishMessageRecoverer(
                rabbitTemplate,
                mailDlx.getName(),
                MailRabbitConfig.MAIL_DLQ_ROUTING_KEY
        );
    }

    @Bean
    public StatelessRetryOperationsInterceptor mailRetryInterceptor(
            MessageRecoverer mailMessageRecoverer
    ) {
        return RetryInterceptorBuilder
                .stateless()
                .maxRetries(4)
                .backOffOptions(
                        1_000L,
                        2.0,
                        10_000L
                )
                .recoverer(mailMessageRecoverer)
                .build();
    }

    @Bean
    public SimpleRabbitListenerContainerFactory mailRabbitListenerContainerFactory(
            ConnectionFactory connectionFactory,
            StatelessRetryOperationsInterceptor mailRetryInterceptor
    ) {
        SimpleRabbitListenerContainerFactory factory =
                new SimpleRabbitListenerContainerFactory();

        factory.setConnectionFactory(connectionFactory);
        factory.setAdviceChain(mailRetryInterceptor);
        factory.setDefaultRequeueRejected(false);

        return factory;
    }
}