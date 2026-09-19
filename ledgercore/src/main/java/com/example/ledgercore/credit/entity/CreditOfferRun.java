package com.example.ledgercore.credit.entity;

import com.example.ledgercore.credit.enums.CreditOfferRunStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(
        name = "credit_offer_runs",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_credit_offer_runs_business_date",
                        columnNames = "business_date"
                )
        },
        indexes = {
                @Index(
                        name = "idx_credit_offer_runs_status",
                        columnList = "status"
                ),
                @Index(
                        name = "idx_credit_offer_runs_business_date",
                        columnList = "business_date"
                )
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreditOfferRun {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(
            name = "business_date",
            nullable = false
    )
    private LocalDate businessDate;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "status",
            nullable = false,
            length = 20
    )
    private CreditOfferRunStatus status;

    @Column(name = "last_processed_id")
    private UUID lastProcessedId;

    @Column(name = "started_at")
    private Instant startedAt;

    @Column(name = "heartbeat_at")
    private Instant heartbeatAt;

    @Column(name = "completed_at")
    private Instant completedAt;

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

    public void start(Instant startedAt) {
        this.status = CreditOfferRunStatus.RUNNING;
        this.startedAt = startedAt;
        this.heartbeatAt = startedAt;
    }

    public void updateProgress(
            UUID lastProcessedId,
            Instant heartbeatAt
    ) {
        this.lastProcessedId = lastProcessedId;
        this.heartbeatAt = heartbeatAt;
    }

    public void complete(Instant completedAt) {
        this.status = CreditOfferRunStatus.COMPLETED;
        this.completedAt = completedAt;
        this.heartbeatAt = completedAt;
    }
}