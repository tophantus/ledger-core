package com.example.ledgercore.webhook.command.repository;

import com.example.ledgercore.webhook.entity.WebhookDelivery;
import com.example.ledgercore.webhook.enums.WebhookDeliveryStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.UUID;

public interface WebhookDeliveryCommandRepository
        extends JpaRepository<WebhookDelivery, UUID> {

    boolean existsByWebhookEndpointIdAndEventId(
            UUID webhookEndpointId,
            UUID eventId
    );

    @Modifying
    @Query("""
            update WebhookDelivery d
            set d.status = :processing,
                d.attemptCount = d.attemptCount + 1,
                d.lastAttemptAt = :now,
                d.lastError = null
            where d.id = :id
              and (
                    d.status = :pending
                    or d.status = :retrying
              )
            """)
    int claim(
            @Param("id") UUID id,
            @Param("processing") WebhookDeliveryStatus processing,
            @Param("pending") WebhookDeliveryStatus pending,
            @Param("retrying") WebhookDeliveryStatus retrying,
            @Param("now") Instant now
    );
}