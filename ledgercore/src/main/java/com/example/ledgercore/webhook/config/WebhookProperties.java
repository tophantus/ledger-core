package com.example.ledgercore.webhook.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.webhook")
public record WebhookProperties(
        boolean allowHttp
) {
}