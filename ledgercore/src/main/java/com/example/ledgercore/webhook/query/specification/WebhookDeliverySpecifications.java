package com.example.ledgercore.webhook.query.specification;

import com.example.ledgercore.webhook.entity.WebhookDelivery;
import com.example.ledgercore.webhook.enums.WebhookDeliveryStatus;
import com.example.ledgercore.webhook.enums.WebhookEventType;
import org.springframework.data.jpa.domain.Specification;

import java.util.UUID;

public final class WebhookDeliverySpecifications {

    private WebhookDeliverySpecifications() {
    }

    public static Specification<WebhookDelivery> hasEndpointId(
            UUID webhookEndpointId
    ) {
        return (root, query, cb) ->
                webhookEndpointId == null
                        ? null
                        : cb.equal(
                        root.get("webhookEndpointId"),
                        webhookEndpointId
                );
    }

    public static Specification<WebhookDelivery> hasStatus(
            WebhookDeliveryStatus status
    ) {
        return (root, query, cb) -> {
            if (status == null) {
                return cb.notEqual(
                        root.get("status"),
                        WebhookDeliveryStatus.PROCESSING
                );
            }

            if (status == WebhookDeliveryStatus.PENDING) {
                return root.get("status").in(
                        WebhookDeliveryStatus.PENDING,
                        WebhookDeliveryStatus.PROCESSING
                );
            }

            return cb.equal(
                    root.get("status"),
                    status
            );
        };
    }

    public static Specification<WebhookDelivery> hasEventType(
            WebhookEventType eventType
    ) {
        return (root, query, cb) ->
                eventType == null
                        ? null
                        : cb.equal(
                        root.get("eventType"),
                        eventType
                );
    }
}