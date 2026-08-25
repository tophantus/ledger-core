package com.example.ledgercore.webhook.command.dto;

import java.util.Set;
import java.util.UUID;

public record RegisterWebhookCommand(
        UUID userId,
        UUID accountId,
        String url,
        Set<String> eventTypes
) {
}