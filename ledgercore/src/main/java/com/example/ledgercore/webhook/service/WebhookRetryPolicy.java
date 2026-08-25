package com.example.ledgercore.webhook.service;

import com.example.ledgercore.webhook.config.WebhookDeliveryProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class WebhookRetryPolicy {

    private final WebhookDeliveryProperties properties;

    public boolean shouldRetry(int attemptCount) {
        return attemptCount < properties.getMaxAttempts();
    }

    public Duration getDelay(int attemptCount) {

        long exponentialDelay =
                properties.getInitialDelaySeconds()
                        * (1L << Math.max(
                        0,
                        attemptCount - 1
                ));

        return Duration.ofSeconds(
                Math.min(
                        exponentialDelay,
                        properties.getMaxDelaySeconds()
                )
        );
    }

    public Duration getProcessingTimeout() {
        return Duration.ofSeconds(
                properties.getProcessingTimeoutSeconds()
        );
    }
}