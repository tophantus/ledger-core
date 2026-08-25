package com.example.ledgercore.webhook.command.dto;

import java.util.UUID;

public record DeleteWebhookCommand(
        UUID userId,
        UUID webhookId
) {
}