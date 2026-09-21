package com.example.ledgercore.hold.entity;

import com.example.ledgercore.common.currency.Currency;
import com.example.ledgercore.hold.enums.CreditHoldReferenceType;
import com.example.ledgercore.hold.enums.CreditHoldStatus;
import com.example.ledgercore.hold.enums.CreditHoldType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "credit_holds",
        indexes = {
                @Index(
                        name = "idx_credit_holds_facility_status",
                        columnList = "credit_facility_id, status"
                ),
                @Index(
                        name = "idx_credit_holds_reference",
                        columnList = "reference_type, reference_id"
                ),
                @Index(
                        name = "idx_credit_holds_expires_at",
                        columnList = "expires_at"
                )
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreditHold {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(
            name = "credit_facility_id",
            nullable = false
    )
    private UUID creditFacilityId;

    @Column(
            nullable = false,
            precision = 19,
            scale = 2
    )
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(
            nullable = false,
            length = 3
    )
    private Currency currency;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "hold_type",
            nullable = false,
            length = 50
    )
    private CreditHoldType holdType;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "reference_type",
            nullable = false,
            length = 50
    )
    private CreditHoldReferenceType referenceType;

    @Column(
            name = "reference_id",
            nullable = false
    )
    private UUID referenceId;

    @Enumerated(EnumType.STRING)
    @Column(
            nullable = false,
            length = 20
    )
    @Builder.Default
    private CreditHoldStatus status =
            CreditHoldStatus.ACTIVE;

    @Column(name = "expires_at")
    private Instant expiresAt;

    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private Instant createdAt;

    @Column(name = "released_at")
    private Instant releasedAt;

    @Version
    @Column(nullable = false)
    @Builder.Default
    private Long version = 0L;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}