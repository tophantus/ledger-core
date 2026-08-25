package com.example.ledgercore.webhook.adapter.inbound.rest;

import com.example.ledgercore.webhook.enums.WebhookStatus;

import java.time.Instant;
import java.util.UUID;

public record UpdateWebhookResponse(
        UUID id,
        UUID accountId,
        String url,
        WebhookStatus status,
        Instant updatedAt
) {
}