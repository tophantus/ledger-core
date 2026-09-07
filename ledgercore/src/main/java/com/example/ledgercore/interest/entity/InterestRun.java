package com.example.ledgercore.interest.entity;

import com.example.ledgercore.interest.enums.InterestRunStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(
        name = "interest_runs",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_interest_runs_business_date",
                        columnNames = "business_date"
                )
        },
        indexes = {
                @Index(
                        name = "idx_interest_runs_status",
                        columnList = "status"
                )
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InterestRun {

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
    private InterestRunStatus status;

    @Column(
            name = "started_at",
            nullable = false
    )
    private Instant startedAt;

    @Column(name = "heartbeat_at")
    private Instant heartbeatAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(
            name = "total_accounts",
            nullable = false
    )
    private Integer totalAccounts;

    @Column(
            name = "successful_accounts",
            nullable = false
    )
    private Integer successfulAccounts;

    @Column(
            name = "failed_accounts",
            nullable = false
    )
    private Integer failedAccounts;

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

        if (heartbeatAt == null) {
            heartbeatAt = startedAt;
        }
    }

    public void heartbeat(Instant heartbeatAt) {
        this.heartbeatAt = heartbeatAt;
    }

    public void complete(
            Instant completedAt
    ) {
        this.status = InterestRunStatus.COMPLETED;
        this.completedAt = completedAt;
        this.heartbeatAt = completedAt;
    }

    public void fail(
            Instant failedAt
    ) {
        this.status = InterestRunStatus.FAILED;
        this.completedAt = failedAt;
        this.heartbeatAt = failedAt;
    }
}