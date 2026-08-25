package com.example.ledgercore.webhook.entity;

import com.example.ledgercore.webhook.enums.WebhookDeliveryStatus;
import com.example.ledgercore.webhook.enums.WebhookEventType;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "webhook_deliveries",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_webhook_deliveries_endpoint_event",
                        columnNames = {
                                "webhook_endpoint_id",
                                "event_id"
                        }
                )
        },
        indexes = {
                @Index(
                        name = "idx_webhook_deliveries_status_next_attempt",
                        columnList = "status, next_attempt_at"
                ),
                @Index(
                        name = "idx_webhook_deliveries_endpoint_id",
                        columnList = "webhook_endpoint_id"
                ),
                @Index(
                        name = "idx_webhook_deliveries_event_id",
                        columnList = "event_id"
                )
        }
)
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WebhookDelivery {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(
            name = "webhook_endpoint_id",
            nullable = false
    )
    private UUID webhookEndpointId;

    @Column(
            name = "event_id",
            nullable = false
    )
    private UUID eventId;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "event_type",
            nullable = false,
            length = 150
    )
    private WebhookEventType eventType;

    @Column(
            name = "payload",
            nullable = false,
            columnDefinition = "TEXT"
    )
    private String payload;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "status",
            nullable = false,
            length = 20
    )
    @Builder.Default
    private WebhookDeliveryStatus status =
            WebhookDeliveryStatus.PENDING;

    @Column(
            name = "attempt_count",
            nullable = false
    )
    @Builder.Default
    private int attemptCount = 0;

    @Column(name = "next_attempt_at")
    private Instant nextAttemptAt;

    @Column(name = "delivered_at")
    private Instant deliveredAt;

    @Column(name = "last_attempt_at")
    private Instant lastAttemptAt;

    @Column(name = "last_error", length = 2000)
    private String lastError;

    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private Instant createdAt;

    @PrePersist
    void prePersist() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    public void markAttempt(Instant attemptedAt) {
        this.attemptCount++;
        this.lastAttemptAt = attemptedAt;
    }

    public void markDelivered(Instant deliveredAt) {
        this.status = WebhookDeliveryStatus.DELIVERED;
        this.deliveredAt = deliveredAt;
        this.nextAttemptAt = null;
        this.lastError = null;
    }

    public void markRetry(
            Instant nextAttemptAt,
            String error
    ) {
        this.status = WebhookDeliveryStatus.RETRYING;
        this.nextAttemptAt = nextAttemptAt;
        this.lastError = error;
    }

    public void markFailed(String error) {
        this.status = WebhookDeliveryStatus.FAILED;
        this.nextAttemptAt = null;
        this.lastError = error;
    }
}