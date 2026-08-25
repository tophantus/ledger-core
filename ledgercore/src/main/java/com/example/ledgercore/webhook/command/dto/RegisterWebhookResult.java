package com.example.ledgercore.webhook.command.dto;

import com.example.ledgercore.webhook.enums.WebhookStatus;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public record RegisterWebhookResult(
        UUID webhookId,
        UUID accountId,
        String url,
        String secret,
        WebhookStatus status,
        Set<String> eventTypes,
        Instant createdAt
) {
}