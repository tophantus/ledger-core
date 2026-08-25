package com.example.ledgercore.webhook.command.dto;

import java.time.Instant;
import java.util.UUID;

public record RotateWebhookSecretResult(
        UUID webhookId,
        String secret,
        Instant rotatedAt
) {
}