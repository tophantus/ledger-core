package com.example.ledgercore.interest.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(
        name = "interest_postings",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_interest_postings_account_period",
                        columnNames = {
                                "account_id",
                                "period_start",
                                "period_end"
                        }
                )
        },
        indexes = {
                @Index(
                        name = "idx_interest_postings_account",
                        columnList = "account_id"
                ),
                @Index(
                        name = "idx_interest_postings_period",
                        columnList = "period_start, period_end"
                ),
                @Index(
                        name = "idx_interest_postings_transaction",
                        columnList = "transaction_id"
                ),
                @Index(
                        name = "idx_interest_postings_run_id",
                        columnList = "run_id"
                )
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InterestPosting {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(
            name = "account_id",
            nullable = false
    )
    private UUID accountId;

    @Column(
            name = "run_id",
            nullable = false
    )
    private UUID runId;

    @Column(
            name = "period_start",
            nullable = false
    )
    private LocalDate periodStart;

    @Column(
            name = "period_end",
            nullable = false
    )
    private LocalDate periodEnd;

    @Column(
            name = "interest_amount",
            nullable = false,
            precision = 19,
            scale = 4
    )
    private BigDecimal interestAmount;

    @Column(name = "transaction_id")
    private UUID transactionId;

    @Column(name = "posted_at")
    private Instant postedAt;

    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}