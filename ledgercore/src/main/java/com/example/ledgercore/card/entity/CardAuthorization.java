package com.example.ledgercore.card.entity;

import com.example.ledgercore.card.enums.CardAuthorizationHoldType;
import com.example.ledgercore.card.enums.CardAuthorizationMethod;
import com.example.ledgercore.card.enums.CardAuthorizationStatus;
import com.example.ledgercore.common.currency.Currency;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "card_authorizations",
        indexes = {
                @Index(
                        name = "idx_card_authorizations_card_id",
                        columnList = "card_id"
                ),
                @Index(
                        name = "idx_card_authorizations_status",
                        columnList = "status"
                )
        }
)
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CardAuthorization {

    @Id
    private UUID id;

    @Column(name = "card_id", nullable = false)
    private UUID cardId;

    @Column(name = "reference", nullable = false, unique = true, length = 100)
    private String reference;

    @Column(name = "merchant_reference", length = 100)
    private String merchantReference;

    @Column(name = "provider_id", nullable = false)
    private UUID providerId;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "authorization_method",
            nullable = false,
            length = 20
    )
    private CardAuthorizationMethod authorizationMethod;

    @Column(
            name = "amount",
            nullable = false,
            precision = 19,
            scale = 2
    )
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "currency", nullable = false, length = 3)
    private Currency currency;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private CardAuthorizationStatus status;

    @Column(name = "hold_id", nullable = false)
    private UUID holdId;

    @Enumerated(EnumType.STRING)
    @Column(name = "hold_type", nullable = false, length = 20)
    private CardAuthorizationHoldType holdType;

    @Column(name = "authorized_at", nullable = false)
    private Instant authorizedAt;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "captured_at")
    private Instant capturedAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public boolean isAuthorized() {
        return status == CardAuthorizationStatus.AUTHORIZED;
    }

    public boolean isExpired(Instant now) {
        return expiresAt != null
                && !expiresAt.isAfter(now);
    }

    public void expire(Instant now) {
        this.status = CardAuthorizationStatus.EXPIRED;
        this.updatedAt = now;
    }
}