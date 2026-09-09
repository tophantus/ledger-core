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
public class OtpMailRabbitConfig {

    public static final String OTP_MAIL_QUEUE =
            "notification.mail.otp.queue";

    public static final String OTP_MAIL_DLX =
            "notification.mail.otp.dlx";

    public static final String OTP_MAIL_DLQ =
            "notification.mail.otp.dlq";

    public static final String OTP_MAIL_DLQ_ROUTING_KEY =
            "notification.mail.otp.dlq";

    @Bean
    public Queue otpMailQueue() {
        return QueueBuilder
                .durable(OTP_MAIL_QUEUE)
                .withArgument(
                        "x-dead-letter-exchange",
                        OTP_MAIL_DLX
                )
                .withArgument(
                        "x-dead-letter-routing-key",
                        OTP_MAIL_DLQ_ROUTING_KEY
                )
                .build();
    }

    @Bean
    public Binding otpMailBinding(
            Queue otpMailQueue,
            TopicExchange otpExchange
    ) {
        return BindingBuilder
                .bind(otpMailQueue)
                .to(otpExchange)
                .with(
                        OtpRabbitConfig
                                .OTP_NOTIFICATION_ROUTING_KEY
                );
    }

    @Bean
    public TopicExchange otpMailDlx() {
        return new TopicExchange(OTP_MAIL_DLX);
    }

    @Bean
    public Queue otpMailDlq() {
        return QueueBuilder
                .durable(OTP_MAIL_DLQ)
                .build();
    }

    @Bean
    public Binding otpMailDlqBinding(
            Queue otpMailDlq,
            TopicExchange otpMailDlx
    ) {
        return BindingBuilder
                .bind(otpMailDlq)
                .to(otpMailDlx)
                .with(
                        OTP_MAIL_DLQ_ROUTING_KEY
                );
    }
}