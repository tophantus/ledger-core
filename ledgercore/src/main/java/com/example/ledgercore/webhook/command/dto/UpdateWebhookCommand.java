package com.example.ledgercore.webhook.command.dto;

import java.util.UUID;

public record UpdateWebhookCommand(
        UUID userId,
        UUID webhookId,
        String url
) {
}