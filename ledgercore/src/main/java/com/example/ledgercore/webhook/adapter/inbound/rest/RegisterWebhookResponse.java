package com.example.ledgercore.webhook.adapter.inbound.rest;

import com.example.ledgercore.webhook.enums.WebhookStatus;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public record RegisterWebhookResponse(
        UUID webhookId,
        UUID accountId,
        String url,
        String secret,
        WebhookStatus status,
        Set<String> eventTypes,
        Instant createdAt
) {
}