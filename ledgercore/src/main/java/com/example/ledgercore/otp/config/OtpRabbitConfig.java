package com.example.ledgercore.otp.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OtpRabbitConfig {

    // =========================
    // OTP EXCHANGE
    // =========================

    public static final String OTP_EXCHANGE =
            "otp.exchange";

    // =========================
    // OTP NOTIFICATION
    // =========================

    public static final String OTP_NOTIFICATION_QUEUE =
            "otp.notification.queue";

    public static final String OTP_NOTIFICATION_ROUTING_KEY =
            "otp.challenge.notification.requested";

    // =========================
    // OTP DEAD LETTER
    // =========================

    public static final String OTP_DLX =
            "otp.dlx";

    public static final String OTP_DLQ =
            "otp.dlq";

    public static final String OTP_DLQ_ROUTING_KEY =
            "otp.dlq";

    // =========================
    // OTP EXCHANGE
    // =========================

    @Bean
    public TopicExchange otpExchange() {
        return new TopicExchange(
                OTP_EXCHANGE
        );
    }

    // =========================
    // OTP NOTIFICATION QUEUE
    // =========================

    @Bean
    public Queue otpNotificationQueue() {
        return QueueBuilder
                .durable(OTP_NOTIFICATION_QUEUE)
                .withArgument(
                        "x-dead-letter-exchange",
                        OTP_DLX
                )
                .withArgument(
                        "x-dead-letter-routing-key",
                        OTP_DLQ_ROUTING_KEY
                )
                .build();
    }

    @Bean
    public Binding otpNotificationBinding() {
        return BindingBuilder
                .bind(otpNotificationQueue())
                .to(otpExchange())
                .with(OTP_NOTIFICATION_ROUTING_KEY);
    }

    // =========================
    // OTP DEAD LETTER EXCHANGE
    // =========================

    @Bean
    public TopicExchange otpDlx() {
        return new TopicExchange(
                OTP_DLX
        );
    }

    // =========================
    // OTP DEAD LETTER QUEUE
    // =========================

    @Bean
    public Queue otpDlq() {
        return QueueBuilder
                .durable(OTP_DLQ)
                .build();
    }

    @Bean
    public Binding otpDlqBinding() {
        return BindingBuilder
                .bind(otpDlq())
                .to(otpDlx())
                .with(OTP_DLQ_ROUTING_KEY);
    }
}