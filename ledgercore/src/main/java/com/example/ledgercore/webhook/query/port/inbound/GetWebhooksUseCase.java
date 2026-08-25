package com.example.ledgercore.webhook.query.port.inbound;

import com.example.ledgercore.webhook.query.dto.WebhookResponse;

import java.util.List;
import java.util.UUID;

public interface GetWebhooksUseCase {

    List<WebhookResponse> execute(
            UUID userId,
            UUID accountId
    );
}