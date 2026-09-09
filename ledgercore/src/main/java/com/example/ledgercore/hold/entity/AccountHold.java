package com.example.ledgercore.hold.entity;

import com.example.ledgercore.hold.enums.AccountHoldReferenceType;
import com.example.ledgercore.hold.enums.AccountHoldStatus;
import com.example.ledgercore.hold.enums.AccountHoldType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "account_holds",
        indexes = {
                @Index(
                        name = "idx_account_holds_account_status",
                        columnList = "account_id, status"
                ),
                @Index(
                        name = "idx_account_holds_reference",
                        columnList = "reference_type, reference_id"
                ),
                @Index(
                        name = "idx_account_holds_expires_at",
                        columnList = "expires_at"
                )
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountHold {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "account_id", nullable = false)
    private UUID accountId;

    @Column(
            nullable = false,
            precision = 19,
            scale = 4
    )
    private BigDecimal amount;

    @Column(
            nullable = false,
            length = 3
    )
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "hold_type",
            nullable = false,
            length = 50
    )
    private AccountHoldType holdType;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "reference_type",
            nullable = false,
            length = 50
    )
    private AccountHoldReferenceType referenceType;

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
    private AccountHoldStatus status =
            AccountHoldStatus.ACTIVE;

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