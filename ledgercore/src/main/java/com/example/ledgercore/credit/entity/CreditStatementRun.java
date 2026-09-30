package com.example.ledgercore.credit.entity;

import com.example.ledgercore.credit.enums.CreditStatementRunStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(
        name = "credit_statement_runs",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_credit_statement_runs_period",
                        columnNames = {
                                "period_start",
                                "period_end"
                        }
                )
        },
        indexes = {
                @Index(
                        name = "idx_credit_statement_runs_status",
                        columnList = "status"
                )
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreditStatementRun {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

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
            name = "statement_date",
            nullable = false
    )
    private LocalDate statementDate;

    @Column(
            name = "due_date",
            nullable = false
    )
    private LocalDate dueDate;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "status",
            nullable = false,
            length = 20
    )
    private CreditStatementRunStatus status;

    @Column(name = "last_processed_facility_id")
    private UUID lastProcessedFacilityId;

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
        this.status = CreditStatementRunStatus.RUNNING;
        this.startedAt = startedAt;
        this.heartbeatAt = startedAt;
    }

    public void updateProgress(
            UUID lastProcessedFacilityId,
            long processedCount,
            Instant heartbeatAt
    ) {
        this.lastProcessedFacilityId = lastProcessedFacilityId;
        this.processedCount = processedCount;
        this.heartbeatAt = heartbeatAt;
    }

    public void complete(Instant completedAt) {
        this.status = CreditStatementRunStatus.COMPLETED;
        this.completedAt = completedAt;
        this.heartbeatAt = completedAt;
    }
}