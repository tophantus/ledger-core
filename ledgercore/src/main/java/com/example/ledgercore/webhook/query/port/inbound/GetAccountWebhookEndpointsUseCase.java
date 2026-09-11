package com.example.ledgercore.webhook.query.port.inbound;

import com.example.ledgercore.common.dto.PageResponse;
import com.example.ledgercore.webhook.query.dto.WebhookResponse;

import java.util.UUID;

public interface GetAccountWebhookEndpointsUseCase {

    PageResponse<WebhookResponse> execute(
            UUID userId,
            UUID accountId,
            int page,
            int size
    );
}