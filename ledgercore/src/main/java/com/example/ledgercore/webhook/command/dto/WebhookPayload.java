package com.example.ledgercore.webhook.command.dto;

import tools.jackson.databind.JsonNode;

import java.util.UUID;

public record WebhookPayload(
        UUID eventId,
        String eventType,
        JsonNode data
) {
}