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
import jakarta.persistence.Version;
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
                ),
                @Index(
                        name = "idx_credit_statements_facility_id",
                        columnList = "credit_facility_id"
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

    @Column(name = "run_id", nullable = false)
    private UUID runId;

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

    @Column(
            name = "paid_amount",
            nullable = false,
            precision = 19,
            scale = 2
    )
    @Builder.Default
    private BigDecimal paidAmount = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private CreditStatementStatus status;

    @Column(name = "next_repayment_attempt_at")
    private Instant nextRepaymentAttemptAt;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public BigDecimal getRemainingAmount() {
        return closingBalance.subtract(paidAmount);
    }

    public boolean isMinimumPaymentSatisfied() {
        return paidAmount.compareTo(minimumPayment) >= 0;
    }

    public boolean isPaid() {
        return paidAmount.compareTo(closingBalance) >= 0;
    }

    public boolean isPastDue(LocalDate date) {
        return date.isAfter(dueDate)
                && !isMinimumPaymentSatisfied();
    }

    public void applyPayment(BigDecimal amount) {
        if (amount == null || amount.signum() <= 0) {
            throw new IllegalArgumentException(
                    "Payment amount must be greater than zero"
            );
        }

        if (status == CreditStatementStatus.PAID
                || status == CreditStatementStatus.NO_PAYMENT_DUE
                || status == CreditStatementStatus.OVERDUE) {
            throw new IllegalStateException(
                    "Statement does not accept payment"
            );
        }

        if (status == CreditStatementStatus.OPEN) {
            throw new IllegalStateException(
                    "Statement has not been issued"
            );
        }

        BigDecimal remainingAmount = getRemainingAmount();

        if (amount.compareTo(remainingAmount) > 0) {
            throw new IllegalArgumentException(
                    "Payment amount exceeds statement remaining amount"
            );
        }

        this.paidAmount = this.paidAmount.add(amount);

        if (isPaid()) {
            this.status = CreditStatementStatus.PAID;
        } else {
            this.status = CreditStatementStatus.PARTIALLY_PAID;
        }
    }

    public void scheduleNextRepaymentAttempt(Instant nextAttemptAt) {
        this.nextRepaymentAttemptAt = nextAttemptAt;
    }

    public void clearNextRepaymentAttempt() {
        this.nextRepaymentAttemptAt = null;
    }
}