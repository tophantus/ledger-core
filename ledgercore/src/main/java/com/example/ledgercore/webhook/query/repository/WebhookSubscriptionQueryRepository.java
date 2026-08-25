package com.example.ledgercore.webhook.query.repository;

import com.example.ledgercore.webhook.entity.WebhookSubscription;
import com.example.ledgercore.webhook.enums.WebhookEventType;
import com.example.ledgercore.webhook.enums.WebhookStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    @Query("""
            select subscription
            from WebhookSubscription subscription
            join WebhookEndpoint endpoint
                on endpoint.id = subscription.webhookEndpointId
            where endpoint.accountId = :accountId
              and endpoint.status = :status
              and subscription.eventType = :eventType
            """)
    List<WebhookSubscription> findActiveSubscriptions(
            @Param("accountId") UUID accountId,
            @Param("eventType") WebhookEventType eventType,
            @Param("status") WebhookStatus status
    );
}