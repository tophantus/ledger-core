package com.example.ledgercore.webhook.adapter.inbound.rest;

import java.time.Instant;
import java.util.UUID;

public record RotateWebhookSecretResponse(
        UUID webhookId,
        String secret,
        Instant rotatedAt
) {
}