package com.example.ledgercore.webhook.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "webhook.delivery.recovery.scheduler")
public class WebhookDeliveryRecoverySchedulerProperties {

    private int batchSize = 100;
}