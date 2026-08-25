package com.example.ledgercore.webhook.adapter.inbound.rest;

import com.example.ledgercore.webhook.enums.WebhookEventType;
import jakarta.validation.constraints.NotEmpty;

import java.util.Set;

public record UpdateWebhookSubscriptionsRequest(
        @NotEmpty
        Set<WebhookEventType> eventTypes
) {
}