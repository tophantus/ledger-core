package com.example.ledgercore.reconciliation.entity;

import com.example.ledgercore.reconciliation.enums.ReconciliationErrorCode;
import com.example.ledgercore.reconciliation.enums.ReconciliationTargetType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "reconciliation_exceptions")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReconciliationException {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = jakarta.persistence.FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "reconciliation_run_id",
            nullable = false
    )
    private ReconciliationRun reconciliationRun;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "target_type",
            nullable = false,
            length = 30
    )
    private ReconciliationTargetType targetType;

    @Column(
            name = "target_id",
            nullable = false
    )
    private UUID targetId;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "error_code",
            nullable = false,
            length = 100
    )
    private ReconciliationErrorCode errorCode;

    @Column(
            name = "expected_value",
            columnDefinition = "TEXT"
    )
    private String expectedValue;

    @Column(
            name = "actual_value",
            columnDefinition = "TEXT"
    )
    private String actualValue;

    @Column(
            name = "message",
            columnDefinition = "TEXT"
    )
    private String message;

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