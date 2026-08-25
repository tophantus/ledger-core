package com.example.ledgercore.webhook.command.dto;

import com.example.ledgercore.webhook.enums.WebhookEventType;

import java.util.Set;
import java.util.UUID;

public record RegisterWebhookCommand(
        UUID userId,
        UUID accountId,
        String url,
        Set<WebhookEventType> eventTypes
) {
}