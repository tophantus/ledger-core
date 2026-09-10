package com.example.ledgercore.webhook.query.dto;

import com.example.ledgercore.webhook.enums.WebhookDeliveryStatus;
import com.example.ledgercore.webhook.enums.WebhookEventType;

import java.time.Instant;
import java.util.UUID;

public record WebhookDeliveryResponse(
        UUID id,
        UUID webhookEndpointId,
        UUID eventId,
        WebhookEventType eventType,
        WebhookDeliveryStatus status,
        int attemptCount,
        Instant nextAttemptAt,
        Instant deliveredAt,
        String lastError,
        Instant createdAt
) {
}