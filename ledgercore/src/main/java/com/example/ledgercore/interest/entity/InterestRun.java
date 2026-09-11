package com.example.ledgercore.interest.entity;

import com.example.ledgercore.interest.enums.InterestRunStatus;
import com.example.ledgercore.interest.enums.InterestRunType;
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
                        name = "uk_interest_runs_business_date_type",
                        columnNames = {
                                "business_date",
                                "run_type"
                        }
                )
        },
        indexes = {
                @Index(
                        name = "idx_interest_runs_status",
                        columnList = "status"
                ),
                @Index(
                        name = "idx_interest_runs_type",
                        columnList = "run_type"
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
            name = "run_type",
            nullable = false,
            length = 20
    )
    private InterestRunType runType;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "status",
            nullable = false,
            length = 20
    )
    private InterestRunStatus status;

    @Column(name = "last_processed_id")
    private UUID lastProcessedId;

    @Column(name = "processed_count")
    private Long processedCount;

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

        if (processedCount == null) {
            processedCount = 0L;
        }
    }

    public void start(Instant startedAt) {
        this.status = InterestRunStatus.RUNNING;
        this.startedAt = startedAt;
        this.heartbeatAt = startedAt;
    }

    public void updateProgress(
            UUID lastProcessedId,
            long processedCount,
            Instant heartbeatAt
    ) {
        this.lastProcessedId = lastProcessedId;
        this.processedCount = processedCount;
        this.heartbeatAt = heartbeatAt;
    }

    public void complete(Instant completedAt) {
        this.status = InterestRunStatus.COMPLETED;
        this.completedAt = completedAt;
        this.heartbeatAt = completedAt;
    }
}