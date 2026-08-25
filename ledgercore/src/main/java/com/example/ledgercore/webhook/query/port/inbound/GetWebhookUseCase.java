package com.example.ledgercore.webhook.query.port.inbound;

import com.example.ledgercore.webhook.query.dto.WebhookResponse;

import java.util.UUID;

public interface GetWebhookUseCase {

    WebhookResponse execute(
            UUID userId,
            UUID webhookId
    );
}