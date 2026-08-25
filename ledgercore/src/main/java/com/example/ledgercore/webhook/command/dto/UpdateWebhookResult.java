package com.example.ledgercore.webhook.command.dto;

import com.example.ledgercore.webhook.enums.WebhookStatus;

import java.time.Instant;
import java.util.UUID;

public record UpdateWebhookResult(
        UUID id,
        UUID accountId,
        String url,
        WebhookStatus status,
        Instant updatedAt
) {
}