package com.example.ledgercore.webhook.adapter.inbound.rest;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.Set;

public record RegisterWebhookRequest(
        @NotBlank
        @Size(max = 2048)
        String url,

        @NotEmpty
        Set<@NotBlank @Size(max = 150) String> eventTypes
) {
}