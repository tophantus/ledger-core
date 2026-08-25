package com.example.ledgercore.webhook.query.dto;

import com.example.ledgercore.webhook.enums.WebhookEventType;
import com.example.ledgercore.webhook.enums.WebhookStatus;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public record WebhookResponse(
        UUID id,
        UUID accountId,
        String url,
        WebhookStatus status,
        Set<WebhookEventType> eventTypes,
        Instant createdAt,
        Instant updatedAt
) {
}