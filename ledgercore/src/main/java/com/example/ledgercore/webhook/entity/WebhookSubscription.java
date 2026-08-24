package com.example.ledgercore.webhook.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "webhook_subscriptions",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_webhook_subscriptions_endpoint_event",
                        columnNames = {
                                "webhook_endpoint_id",
                                "event_type"
                        }
                )
        },
        indexes = {
                @Index(
                        name = "idx_webhook_subscriptions_endpoint_id",
                        columnList = "webhook_endpoint_id"
                ),
                @Index(
                        name = "idx_webhook_subscriptions_event_type",
                        columnList = "event_type"
                )
        }
)
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WebhookSubscription {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(
            name = "webhook_endpoint_id",
            nullable = false
    )
    private UUID webhookEndpointId;

    @Column(
            name = "event_type",
            nullable = false,
            length = 150
    )
    private String eventType;

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
}