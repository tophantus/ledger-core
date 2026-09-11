package com.example.ledgercore.webhook.adapter.inbound.rest;

import com.example.ledgercore.webhook.enums.WebhookEventType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.Set;
import java.util.UUID;

public record RegisterWebhookRequest(
        @NotNull
        UUID accountId,

        @NotBlank
        @Size(max = 2048)
        String url,

        @NotEmpty
        Set<WebhookEventType> eventTypes
) {
}