package com.example.ledgercore.webhook.command.dto;

import com.example.ledgercore.webhook.enums.WebhookStatus;

import java.util.UUID;

public record UpdateWebhookCommand(
        UUID userId,
        UUID webhookId,
        String url,
        WebhookStatus status
) {
}