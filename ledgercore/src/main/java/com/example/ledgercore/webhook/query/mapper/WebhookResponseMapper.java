package com.example.ledgercore.webhook.query.mapper;

import com.example.ledgercore.webhook.entity.WebhookEndpoint;
import com.example.ledgercore.webhook.entity.WebhookSubscription;
import com.example.ledgercore.webhook.enums.WebhookEventType;
import com.example.ledgercore.webhook.query.dto.WebhookResponse;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class WebhookResponseMapper {

    public WebhookResponse map(
            WebhookEndpoint endpoint,
            List<WebhookSubscription> subscriptions
    ) {
        Set<WebhookEventType> eventTypes = subscriptions.stream()
                .map(WebhookSubscription::getEventType)
                .collect(Collectors.toSet());

        return new WebhookResponse(
                endpoint.getId(),
                endpoint.getAccountId(),
                endpoint.getUrl(),
                endpoint.getStatus(),
                eventTypes,
                endpoint.getCreatedAt(),
                endpoint.getUpdatedAt()
        );
    }

    public List<WebhookResponse> map(
            List<WebhookEndpoint> endpoints,
            List<WebhookSubscription> subscriptions
    ) {
        var subscriptionsByEndpoint = subscriptions.stream()
                .collect(Collectors.groupingBy(
                        WebhookSubscription::getWebhookEndpointId
                ));

        return endpoints.stream()
                .map(endpoint -> map(
                        endpoint,
                        subscriptionsByEndpoint.getOrDefault(
                                endpoint.getId(),
                                List.of()
                        )
                ))
                .toList();
    }
}