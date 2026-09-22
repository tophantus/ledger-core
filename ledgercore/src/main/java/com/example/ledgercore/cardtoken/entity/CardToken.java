package com.example.ledgercore.cardtoken.entity;

import com.example.ledgercore.cardtoken.enums.CardTokenStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "card_tokens",
        indexes = {
                @Index(
                        name = "idx_card_tokens_card_id",
                        columnList = "card_id"
                ),
                @Index(
                        name = "idx_card_tokens_provider_id",
                        columnList = "provider_id"
                )
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CardToken {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(
            name = "token",
            nullable = false,
            unique = true
    )
    private String token;

    @Column(
            name = "card_id",
            nullable = false
    )
    private UUID cardId;

    @Column(
            name = "provider_id",
            nullable = false
    )
    private UUID providerId;

    @Column(
            name = "provider_customer_reference",
            nullable = false,
            length = 255
    )
    private String providerCustomerReference;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "status",
            nullable = false,
            length = 20
    )
    private CardTokenStatus status;

    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private Instant createdAt;

    @Column(name = "activated_at")
    private Instant activatedAt;

    @Column(name = "suspended_at")
    private Instant suspendedAt;

    @Column(name = "revoked_at")
    private Instant revokedAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now();

        if (createdAt == null) {
            createdAt = now;
        }

        if (updatedAt == null) {
            updatedAt = now;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
}