package com.example.ledgercore.webhook.command.dto;

import java.util.UUID;

public record RotateWebhookSecretCommand(
        UUID userId,
        UUID webhookId
) {
}