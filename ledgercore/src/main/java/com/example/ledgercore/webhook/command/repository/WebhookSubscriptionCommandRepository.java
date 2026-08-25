package com.example.ledgercore.webhook.command.repository;

import com.example.ledgercore.webhook.entity.WebhookSubscription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface WebhookSubscriptionCommandRepository
        extends JpaRepository<WebhookSubscription, UUID> {
    void deleteAllByWebhookEndpointId(UUID webhookEndpointId);
}