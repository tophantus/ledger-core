package com.example.ledgercore.interest.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(
        name = "credit_interest_accruals",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_credit_interest_accruals_facility_date",
                        columnNames = {
                                "credit_facility_id",
                                "business_date"
                        }
                )
        },
        indexes = {
                @Index(
                        name = "idx_credit_interest_accruals_facility_date",
                        columnList = "credit_facility_id, business_date"
                ),
                @Index(
                        name = "idx_credit_interest_accruals_business_date",
                        columnList = "business_date"
                ),
                @Index(
                        name = "idx_credit_interest_accruals_posting_id",
                        columnList = "posting_id"
                ),
                @Index(
                        name = "idx_credit_interest_accruals_run_id",
                        columnList = "run_id"
                )
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreditInterestAccrual {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "credit_facility_id", nullable = false)
    private UUID creditFacilityId;

    @Column(name = "business_date", nullable = false)
    private LocalDate businessDate;

    @Column(
            name = "principal_amount",
            nullable = false,
            precision = 19,
            scale = 4
    )
    private BigDecimal principalAmount;

    @Column(
            name = "interest_rate",
            nullable = false,
            precision = 10,
            scale = 6
    )
    private BigDecimal interestRate;

    @Column(
            name = "interest_amount",
            nullable = false,
            precision = 19,
            scale = 4
    )
    private BigDecimal interestAmount;

    @Column(name = "journal_entry_id")
    private UUID journalEntryId;

    @Column(name = "posting_id")
    private UUID postingId;

    @Column(name = "run_id", nullable = false)
    private UUID runId;

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
