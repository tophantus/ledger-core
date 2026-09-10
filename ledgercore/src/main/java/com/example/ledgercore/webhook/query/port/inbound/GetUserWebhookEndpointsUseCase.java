package com.example.ledgercore.webhook.query.port.inbound;

import com.example.ledgercore.common.dto.PageResponse;
import com.example.ledgercore.webhook.query.dto.WebhookResponse;

import java.util.UUID;

public interface GetUserWebhookEndpointsUseCase {

    PageResponse<WebhookResponse> execute(
            UUID userId,
            int page,
            int size
    );
}