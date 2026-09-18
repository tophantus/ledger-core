package com.example.ledgercore.credit.entity;

import com.example.ledgercore.credit.enums.CreditStatementStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(
        name = "credit_statements",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_credit_statements_period",
                        columnNames = {
                                "credit_facility_id",
                                "period_start",
                                "period_end"
                        }
                )
        },
        indexes = {
                @Index(
                        name = "idx_credit_statements_due_date",
                        columnList = "due_date"
                )
        }
)
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreditStatement {

    @Id
    private UUID id;

    @Column(name = "credit_facility_id", nullable = false)
    private UUID creditFacilityId;

    @Column(name = "period_start", nullable = false)
    private LocalDate periodStart;

    @Column(name = "period_end", nullable = false)
    private LocalDate periodEnd;

    @Column(name = "statement_date", nullable = false)
    private LocalDate statementDate;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Column(
            name = "opening_balance",
            nullable = false,
            precision = 19,
            scale = 2
    )
    private BigDecimal openingBalance;

    @Column(
            name = "purchases_amount",
            nullable = false,
            precision = 19,
            scale = 2
    )
    private BigDecimal purchasesAmount;

    @Column(
            name = "payments_amount",
            nullable = false,
            precision = 19,
            scale = 2
    )
    private BigDecimal paymentsAmount;

    @Column(
            name = "fees_amount",
            nullable = false,
            precision = 19,
            scale = 2
    )
    private BigDecimal feesAmount;

    @Column(
            name = "interest_amount",
            nullable = false,
            precision = 19,
            scale = 2
    )
    private BigDecimal interestAmount;

    @Column(
            name = "closing_balance",
            nullable = false,
            precision = 19,
            scale = 2
    )
    private BigDecimal closingBalance;

    @Column(
            name = "minimum_payment",
            nullable = false,
            precision = 19,
            scale = 2
    )
    private BigDecimal minimumPayment;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private CreditStatementStatus status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}