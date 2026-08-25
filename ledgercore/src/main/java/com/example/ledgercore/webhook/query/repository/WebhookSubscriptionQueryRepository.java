package com.example.ledgercore.webhook.query.repository;

import com.example.ledgercore.webhook.entity.WebhookSubscription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface WebhookSubscriptionQueryRepository
        extends JpaRepository<WebhookSubscription, UUID> {

    List<WebhookSubscription> findAllByWebhookEndpointId(
            UUID webhookEndpointId
    );

    List<WebhookSubscription> findAllByWebhookEndpointIdIn(
            List<UUID> webhookEndpointIds
    );
}