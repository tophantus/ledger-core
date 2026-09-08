package com.example.ledgercore.withdrawal.entity;

import com.example.ledgercore.withdrawal.enums.WithdrawalRequestStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "withdrawal_requests",
        indexes = {
                @Index(
                        name = "idx_withdrawal_requests_user_id",
                        columnList = "user_id"
                ),
                @Index(
                        name = "idx_withdrawal_requests_account_id",
                        columnList = "account_id"
                ),
                @Index(
                        name = "idx_withdrawal_requests_status",
                        columnList = "status"
                ),
                @Index(
                        name = "idx_withdrawal_requests_expires_at",
                        columnList = "expires_at"
                )
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WithdrawalRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

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
            nullable = false,
            length = 20
    )
    @Builder.Default
    private WithdrawalRequestStatus status =
            WithdrawalRequestStatus.PENDING;

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

    @Column(name = "confirmed_at")
    private Instant confirmedAt;

    @Version
    @Column(nullable = false)
    @Builder.Default
    private Long version = 0L;

    public boolean isPending() {
        return status == WithdrawalRequestStatus.PENDING;
    }

    public boolean isExpired(Instant now) {
        return now.isAfter(expiresAt);
    }

    public void confirm(Instant now) {
        this.status = WithdrawalRequestStatus.CONFIRMED;
        this.confirmedAt = now;
    }

    public void expire() {
        this.status = WithdrawalRequestStatus.EXPIRED;
    }

    public void cancel() {
        this.status = WithdrawalRequestStatus.CANCELLED;
    }
}