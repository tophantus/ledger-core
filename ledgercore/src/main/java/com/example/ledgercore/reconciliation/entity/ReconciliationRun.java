package com.example.ledgercore.reconciliation.entity;

import com.example.ledgercore.reconciliation.enums.ReconciliationRunStatus;
import com.example.ledgercore.reconciliation.enums.ReconciliationType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "reconciliation_runs")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReconciliationRun {

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
            name = "type",
            nullable = false,
            length = 50
    )
    private ReconciliationType type;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "status",
            nullable = false,
            length = 20
    )
    private ReconciliationRunStatus status;

    @Column(name = "last_processed_id")
    private UUID lastProcessedId;

    @Column(
            name = "processed_count",
            nullable = false
    )
    @Builder.Default
    private long processedCount = 0L;

    @Column(name = "started_at")
    private Instant startedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private Instant createdAt;

    @Column(
            name = "updated_at",
            nullable = false
    )
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now();

        if (createdAt == null) {
            createdAt = now;
        }

        if (updatedAt == null) {
            updatedAt = now;
        }

        if (status == null) {
            status = ReconciliationRunStatus.PENDING;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

    public void start(Instant startedAt) {
        this.status = ReconciliationRunStatus.RUNNING;
        this.startedAt = startedAt;
    }

    public void updateProgress(
            UUID lastProcessedId,
            long processedCount
    ) {
        if (processedCount < 0) {
            throw new IllegalArgumentException(
                    "processedCount must not be negative"
            );
        }

        this.lastProcessedId = lastProcessedId;
        this.processedCount = processedCount;
    }

    public void complete(Instant completedAt) {
        this.status = ReconciliationRunStatus.COMPLETED;
        this.completedAt = completedAt;
    }
}