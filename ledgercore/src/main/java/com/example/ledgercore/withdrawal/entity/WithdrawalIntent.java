package com.example.ledgercore.withdrawal.entity;

import com.example.ledgercore.withdrawal.enums.WithdrawalIntentStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "withdrawal_intents",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_withdrawal_intents_request_id",
                        columnNames = "withdrawal_request_id"
                ),
                @UniqueConstraint(
                        name = "uk_withdrawal_intents_reference",
                        columnNames = "withdrawal_reference"
                ),
                @UniqueConstraint(
                        name = "uk_withdrawal_intents_hold_id",
                        columnNames = "hold_id"
                )
        },
        indexes = {
                @Index(
                        name = "idx_withdrawal_intents_user_id",
                        columnList = "user_id"
                ),
                @Index(
                        name = "idx_withdrawal_intents_account_id",
                        columnList = "account_id"
                ),
                @Index(
                        name = "idx_withdrawal_intents_status",
                        columnList = "status"
                ),
                @Index(
                        name = "idx_withdrawal_intents_expires_at",
                        columnList = "expires_at"
                )
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WithdrawalIntent {

    @Id
    private UUID id;

    @Column(
            name = "withdrawal_request_id",
            nullable = false
    )
    private UUID withdrawalRequestId;

    @Column(
            name = "withdrawal_reference",
            nullable = false,
            length = 50
    )
    private String withdrawalReference;

    @Column(
            name = "user_id",
            nullable = false
    )
    private UUID userId;

    @Column(
            name = "account_id",
            nullable = false
    )
    private UUID accountId;

    @Column(
            name = "hold_id",
            nullable = false
    )
    private UUID holdId;

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
            name = "withdrawal_code_hash",
            nullable = false,
            length = 255
    )
    private String withdrawalCodeHash;

    @Enumerated(EnumType.STRING)
    @Column(
            nullable = false,
            length = 20
    )
    @Builder.Default
    private WithdrawalIntentStatus status =
            WithdrawalIntentStatus.READY;

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

    @Version
    @Column(nullable = false)
    @Builder.Default
    private Long version = 0L;

    public boolean isReady() {
        return status == WithdrawalIntentStatus.READY;
    }

    public boolean isExpired(Instant now) {
        return !now.isBefore(expiresAt);
    }

    public void complete(Instant now) {
        this.status = WithdrawalIntentStatus.COMPLETED;
        this.completedAt = now;
    }

    public void expire() {
        this.status = WithdrawalIntentStatus.EXPIRED;
    }

    public void cancel() {
        this.status = WithdrawalIntentStatus.CANCELLED;
    }
}