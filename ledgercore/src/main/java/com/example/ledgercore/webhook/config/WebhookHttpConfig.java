package com.example.ledgercore.webhook.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class WebhookHttpConfig {

    @Bean
    public RestClient webhookRestClient(
            RestClient.Builder builder
    ) {
        return builder
                .build();
    }
}