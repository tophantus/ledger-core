package com.example.ledgercore.webhook.query.dto;

import com.example.ledgercore.webhook.enums.WebhookEventType;

import java.util.UUID;

public record WebhookSubscriptionResponse(
        UUID id,
        WebhookEventType eventType
) {
}