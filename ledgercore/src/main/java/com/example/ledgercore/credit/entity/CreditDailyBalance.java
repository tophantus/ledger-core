package com.example.ledgercore.credit.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
        name = "credit_daily_balances",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_credit_daily_balances_date",
                        columnNames = {
                                "credit_facility_id",
                                "business_date"
                        }
                )
        },
        indexes = {
                @Index(
                        name = "idx_credit_daily_balances_business_date",
                        columnList = "business_date"
                )
        }
)
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreditDailyBalance {

    @Id
    private UUID id;

    @Column(name = "credit_facility_id", nullable = false)
    private UUID creditFacilityId;

    @Column(name = "business_date", nullable = false)
    private LocalDate businessDate;

    @Column(
            name = "closing_balance",
            nullable = false,
            precision = 19,
            scale = 2
    )
    private BigDecimal closingBalance;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
}