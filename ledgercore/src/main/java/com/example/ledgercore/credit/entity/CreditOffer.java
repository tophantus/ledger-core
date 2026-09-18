package com.example.ledgercore.credit.entity;

import com.example.ledgercore.common.currency.Currency;
import com.example.ledgercore.credit.enums.CreditOfferStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "credit_offers",
        indexes = {
                @Index(
                        name = "idx_credit_offers_customer_id",
                        columnList = "customer_id"
                ),
                @Index(
                        name = "idx_credit_offers_credit_facility_id",
                        columnList = "credit_facility_id"
                ),
                @Index(
                        name = "idx_credit_offers_status",
                        columnList = "status"
                ),
                @Index(
                        name = "idx_credit_offers_expires_at",
                        columnList = "expires_at"
                ),
                @Index(
                        name = "idx_credit_offers_customer_status",
                        columnList = "customer_id, status"
                )
        }
)
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreditOffer {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "customer_id", nullable = false)
    private UUID customerId;

    @Column(name = "credit_facility_id")
    private UUID creditFacilityId;

    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @Column(
            name = "approved_limit",
            nullable = false,
            precision = 19,
            scale = 2
    )
    private BigDecimal approvedLimit;

    @Enumerated(EnumType.STRING)
    @Column(name = "currency", nullable = false, length = 3)
    private Currency currency;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private CreditOfferStatus status;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "accepted_at")
    private Instant acceptedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public void accept(
            UUID creditFacilityId,
            Instant acceptedAt
    ) {
        this.creditFacilityId = creditFacilityId;
        this.status = CreditOfferStatus.ACCEPTED;
        this.acceptedAt = acceptedAt;
        this.updatedAt = acceptedAt;
    }
}