package com.example.ledgercore.credit.entity;

import com.example.ledgercore.common.currency.Currency;
import com.example.ledgercore.credit.enums.CreditFacilityStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "credit_facilities",
        indexes = {
                @Index(
                        name = "idx_credit_facilities_customer_id",
                        columnList = "customer_id"
                ),
                @Index(
                        name = "idx_credit_facilities_product_id",
                        columnList = "product_id"
                )
        }
)
@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreditFacility {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "customer_id", nullable = false)
    private UUID customerId;

    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @Column(
            name = "credit_limit",
            nullable = false,
            precision = 19,
            scale = 2
    )
    private BigDecimal creditLimit;

    @Column(
            name = "outstanding_balance",
            nullable = false,
            precision = 19,
            scale = 2
    )
    private BigDecimal outstandingBalance;

    @Column(
            name = "hold_amount",
            nullable = false,
            precision = 19,
            scale = 2
    )
    @Builder.Default
    private BigDecimal holdAmount = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "currency", nullable = false, length = 3)
    private Currency currency;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private CreditFacilityStatus status;

    @Column(name = "opened_at", nullable = false)
    private Instant openedAt;

    @Column(name = "closed_at")
    private Instant closedAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    public BigDecimal getAvailableCredit() {
        return creditLimit
                .subtract(outstandingBalance)
                .subtract(holdAmount);
    }

    public void updateCreditTerms(
            UUID productId,
            BigDecimal creditLimit,
            Currency currency,
            Instant updatedAt
    ) {
        this.productId = productId;
        this.creditLimit = creditLimit;
        this.currency = currency;
        this.updatedAt = updatedAt;
    }
}