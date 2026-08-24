package com.example.ledgercore.otp.config;

import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OtpRabbitConfig {

    public static final String OTP_EXCHANGE = "otp.exchange";

    public static final String OTP_NOTIFICATION_ROUTING_KEY =
            "otp.challenge.notification.requested";

    @Bean
    public TopicExchange otpExchange() {
        return new TopicExchange(OTP_EXCHANGE);
    }
}