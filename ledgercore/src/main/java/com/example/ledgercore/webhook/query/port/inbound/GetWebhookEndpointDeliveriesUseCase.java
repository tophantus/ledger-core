package com.example.ledgercore.webhook.query.port.inbound;

import com.example.ledgercore.common.dto.PageResponse;
import com.example.ledgercore.webhook.query.dto.GetWebhookEndpointDeliveriesQuery;
import com.example.ledgercore.webhook.query.dto.WebhookDeliveryResponse;

public interface GetWebhookEndpointDeliveriesUseCase {

    PageResponse<WebhookDeliveryResponse> execute(
            GetWebhookEndpointDeliveriesQuery query
    );
}