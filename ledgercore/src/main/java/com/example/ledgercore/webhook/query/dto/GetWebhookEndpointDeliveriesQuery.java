package com.example.ledgercore.webhook.query.dto;

import com.example.ledgercore.webhook.enums.WebhookDeliveryStatus;
import com.example.ledgercore.webhook.enums.WebhookEventType;

import java.util.UUID;

public record GetWebhookEndpointDeliveriesQuery(
        UUID userId,
        UUID webhookEndpointId,
        WebhookDeliveryStatus status,
        WebhookEventType eventType,
        int page,
        int size
) {
    public GetWebhookEndpointDeliveriesQuery {
        page = Math.max(page, 0);
        size = Math.clamp(size, 1, 100);
    }
}