package com.example.ledgercore.webhook.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "webhook.delivery")
public class WebhookDeliveryProperties {

    private int maxAttempts = 5;

    private long initialDelaySeconds = 5;

    private long maxDelaySeconds = 300;

    private long processingTimeoutSeconds = 60;
}