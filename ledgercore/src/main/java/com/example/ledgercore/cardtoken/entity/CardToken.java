package com.example.ledgercore.cardtoken.entity;

import com.example.ledgercore.cardtoken.enums.CardTokenStatus;
import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.common.exception.ErrorCode;
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

    public void activate(Instant now) {
        if (status != CardTokenStatus.PENDING) {
            throw new BusinessException(
                    ErrorCode.CARD_TOKEN_INVALID_STATUS
            );
        }

        status = CardTokenStatus.ACTIVE;
        activatedAt = now;
    }

    public void suspend(Instant now) {
        if (status != CardTokenStatus.ACTIVE) {
            throw new BusinessException(
                    ErrorCode.CARD_TOKEN_INVALID_STATUS
            );
        }

        status = CardTokenStatus.SUSPENDED;
        suspendedAt = now;
    }

    public void resume(Instant now) {
        if (status != CardTokenStatus.SUSPENDED) {
            throw new BusinessException(
                    ErrorCode.CARD_TOKEN_INVALID_STATUS
            );
        }

        status = CardTokenStatus.ACTIVE;
        suspendedAt = null;
    }

    public void revoke(Instant now) {
        if (status == CardTokenStatus.REVOKED) {
            return;
        }

        status = CardTokenStatus.REVOKED;
        revokedAt = now;
    }

    public boolean isActive() {
        return status == CardTokenStatus.ACTIVE;
    }

    public boolean isSuspended() {
        return status == CardTokenStatus.SUSPENDED;
    }

    public boolean isRevoked() {
        return status == CardTokenStatus.REVOKED;
    }

    public boolean canBeUsed() {
        return status == CardTokenStatus.ACTIVE;
    }

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