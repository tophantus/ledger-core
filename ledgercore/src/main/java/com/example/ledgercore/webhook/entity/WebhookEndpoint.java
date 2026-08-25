package com.example.ledgercore.webhook.entity;

import com.example.ledgercore.webhook.enums.WebhookStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "webhook_endpoints",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_webhook_endpoints_account_url",
                        columnNames = {
                                "account_id",
                                "url"
                        }
                )
        },
        indexes = {
                @Index(
                        name = "idx_webhook_endpoints_account_id",
                        columnList = "account_id"
                ),
                @Index(
                        name = "idx_webhook_endpoints_status",
                        columnList = "status"
                )
        }
)
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WebhookEndpoint {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(
            name = "account_id",
            nullable = false
    )
    private UUID accountId;

    @Column(
            name = "url",
            nullable = false,
            length = 2048
    )
    private String url;

    @Column(
            name = "secret",
            nullable = false,
            length = 255
    )
    private String secret;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "status",
            nullable = false,
            length = 20
    )
    @Builder.Default
    private WebhookStatus status = WebhookStatus.ACTIVE;

    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private Instant createdAt;

    @Column(
            name = "updated_at",
            nullable = false
    )
    private Instant updatedAt;

    public void updateUrl(String url) {
        this.url = url;
    }

    public void updateStatus(WebhookStatus status) {
        this.status = status;
    }

    public void rotateSecret(String secret) {
        this.secret = secret;
    }

    public void inactivate() {
        this.status = WebhookStatus.INACTIVE;
    }

    @PrePersist
    void prePersist() {
        Instant now = Instant.now();

        if (createdAt == null) {
            createdAt = now;
        }

        if (updatedAt == null) {
            updatedAt = now;
        }
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = Instant.now();
    }
}