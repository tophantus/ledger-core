package com.example.ledgercore.webhook.adapter.inbound.rest;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record UpdateWebhookRequest(
        @NotBlank
        @Pattern(
                regexp = "^https://.+",
                message = "Webhook URL must use HTTPS"
        )
        String url
) {
}