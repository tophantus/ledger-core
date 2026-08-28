package com.example.ledgercore.transaction.entity;

import com.example.ledgercore.transaction.enums.TransferIntentStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "transfer_intents",
        indexes = {
                @Index(
                        name = "idx_transfer_intents_user_id",
                        columnList = "user_id"
                ),
                @Index(
                        name = "idx_transfer_intents_status",
                        columnList = "status"
                ),
                @Index(
                        name = "idx_transfer_intents_expires_at",
                        columnList = "expires_at"
                )
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransferIntent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(
            name = "user_id",
            nullable = false
    )
    private UUID userId;

    @Column(
            name = "source_account_id",
            nullable = false
    )
    private UUID sourceAccountId;

    @Column(
            name = "destination_account_id",
            nullable = false
    )
    private UUID destinationAccountId;

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

    @Column(
            nullable = false,
            length = 50
    )
    private String reference;

    @Column(length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(
            nullable = false,
            length = 20
    )
    @Builder.Default
    private TransferIntentStatus status =
            TransferIntentStatus.PENDING;

    @Column(
            name = "expires_at",
            nullable = false
    )
    private Instant expiresAt;

    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private Instant createdAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @PrePersist
    protected void prePersist() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    public boolean isExpired(Instant now) {
        return now.isAfter(expiresAt);
    }

    public boolean isPending() {
        return status == TransferIntentStatus.PENDING;
    }

    public void complete(Instant now) {
        this.status = TransferIntentStatus.COMPLETED;
        this.completedAt = now;
    }

    public void expire() {
        this.status = TransferIntentStatus.EXPIRED;
    }

    public void cancel() {
        this.status = TransferIntentStatus.CANCELLED;
    }
}