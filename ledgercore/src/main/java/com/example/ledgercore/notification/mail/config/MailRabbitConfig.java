package com.example.ledgercore.notification.mail.config;

import com.example.ledgercore.otp.config.OtpRabbitConfig;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MailRabbitConfig {

    public static final String MAIL_QUEUE =
            "notification.mail.queue";

    public static final String MAIL_DLX =
            "notification.mail.dlx";

    public static final String MAIL_DLQ =
            "notification.mail.dlq";

    public static final String MAIL_DLQ_ROUTING_KEY =
            "notification.mail.dlq";

    @Bean
    public Queue mailQueue() {
        return QueueBuilder
                .durable(MAIL_QUEUE)
                .withArgument(
                        "x-dead-letter-exchange",
                        MAIL_DLX
                )
                .withArgument(
                        "x-dead-letter-routing-key",
                        MAIL_DLQ_ROUTING_KEY
                )
                .build();
    }

    @Bean
    public Binding mailBinding(
            Queue mailQueue,
            TopicExchange otpExchange
    ) {
        return BindingBuilder
                .bind(mailQueue)
                .to(otpExchange)
                .with(
                        OtpRabbitConfig
                                .OTP_NOTIFICATION_ROUTING_KEY
                );
    }

    @Bean
    public TopicExchange mailDlx() {
        return new TopicExchange(MAIL_DLX);
    }

    @Bean
    public Queue mailDlq() {
        return QueueBuilder
                .durable(MAIL_DLQ)
                .build();
    }

    @Bean
    public Binding mailDlqBinding(
            Queue mailDlq,
            TopicExchange mailDlx
    ) {
        return BindingBuilder
                .bind(mailDlq)
                .to(mailDlx)
                .with(MAIL_DLQ_ROUTING_KEY);
    }
}