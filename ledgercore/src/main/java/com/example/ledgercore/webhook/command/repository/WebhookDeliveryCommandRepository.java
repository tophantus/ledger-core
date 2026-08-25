package com.example.ledgercore.webhook.command.repository;

import com.example.ledgercore.webhook.entity.WebhookDelivery;
import com.example.ledgercore.webhook.enums.WebhookDeliveryStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

public interface WebhookDeliveryCommandRepository
        extends JpaRepository<WebhookDelivery, UUID> {

    boolean existsByWebhookEndpointIdAndEventId(
            UUID webhookEndpointId,
            UUID eventId
    );

    @Modifying
    @Transactional
    @Query("""
            update WebhookDelivery d
               set d.status = :processingStatus,
                   d.attemptCount = d.attemptCount + 1,
                   d.attemptStartedAt = :now,
                   d.nextAttemptAt = null,
                   d.lastError = null
             where d.id = :id
               and d.status in (:pendingStatus, :retryingStatus)
            """)
    int claim(
            @Param("id") UUID id,
            @Param("processingStatus")
            WebhookDeliveryStatus processingStatus,
            @Param("pendingStatus")
            WebhookDeliveryStatus pendingStatus,
            @Param("retryingStatus")
            WebhookDeliveryStatus retryingStatus,
            @Param("now") Instant now
    );

    @Modifying
    @Transactional
    @Query("""
            update WebhookDelivery d
               set d.status = :status,
                   d.attemptStartedAt = null,
                   d.deliveredAt = :deliveredAt,
                   d.nextAttemptAt = null,
                   d.lastError = null
             where d.id = :id
               and d.status = :processingStatus
            """)
    int markDelivered(
            @Param("id") UUID id,
            @Param("status")
            WebhookDeliveryStatus status,
            @Param("processingStatus")
            WebhookDeliveryStatus processingStatus,
            @Param("deliveredAt") Instant deliveredAt
    );

    @Modifying
    @Transactional
    @Query("""
            update WebhookDelivery d
               set d.status = :retryingStatus,
                   d.attemptStartedAt = null,
                   d.nextAttemptAt = :nextAttemptAt,
                   d.lastError = :error
             where d.id = :id
               and d.status = :processingStatus
            """)
    int markRetry(
            @Param("id") UUID id,
            @Param("retryingStatus")
            WebhookDeliveryStatus retryingStatus,
            @Param("processingStatus")
            WebhookDeliveryStatus processingStatus,
            @Param("nextAttemptAt") Instant nextAttemptAt,
            @Param("error") String error
    );

    @Modifying
    @Transactional
    @Query("""
            update WebhookDelivery d
               set d.status = :failedStatus,
                   d.attemptStartedAt = null,
                   d.nextAttemptAt = null,
                   d.lastError = :error
             where d.id = :id
               and d.status = :processingStatus
            """)
    int markFailed(
            @Param("id") UUID id,
            @Param("failedStatus")
            WebhookDeliveryStatus failedStatus,
            @Param("processingStatus")
            WebhookDeliveryStatus processingStatus,
            @Param("error") String error
    );

    @Modifying
    @Transactional
    @Query("""
            update WebhookDelivery d
               set d.status = :retryingStatus,
                   d.attemptStartedAt = null,
                   d.nextAttemptAt = :nextAttemptAt,
                   d.lastError = :error
             where d.id = :id
               and d.status = :processingStatus
            """)
    int recoverStaleProcessing(
            @Param("id") UUID id,
            @Param("processingStatus")
            WebhookDeliveryStatus processingStatus,
            @Param("retryingStatus")
            WebhookDeliveryStatus retryingStatus,
            @Param("nextAttemptAt") Instant nextAttemptAt,
            @Param("error") String error
    );
}