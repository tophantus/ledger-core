package com.example.ledgercore.webhook.adapter.inbound.rest;

import com.example.ledgercore.webhook.enums.WebhookStatus;
import jakarta.validation.constraints.Pattern;

public record UpdateWebhookRequest(
        @Pattern(
                regexp = "^https://.+",
                message = "Webhook URL must use HTTPS"
        )
        String url,

        WebhookStatus status
) {
}