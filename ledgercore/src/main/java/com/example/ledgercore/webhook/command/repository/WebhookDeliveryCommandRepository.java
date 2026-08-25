package com.example.ledgercore.webhook.command.repository;

import com.example.ledgercore.webhook.entity.WebhookDelivery;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface WebhookDeliveryCommandRepository
        extends JpaRepository<WebhookDelivery, UUID> {
    boolean existsByWebhookEndpointIdAndEventId(
            UUID webhookEndpointId,
            UUID eventId
    );
}