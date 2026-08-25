package com.example.ledgercore.webhook.command.dto;

import com.example.ledgercore.webhook.enums.WebhookEventType;

import java.util.Set;
import java.util.UUID;

public record UpdateWebhookSubscriptionsCommand(
        UUID userId,
        UUID webhookId,
        Set<WebhookEventType> eventTypes
) {
}