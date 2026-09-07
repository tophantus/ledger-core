package com.example.ledgercore.interest.entity;

import com.example.ledgercore.interest.enums.DayCountConvention;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(
        name = "interest_configs",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_interest_configs_product_currency_effective_from",
                        columnNames = {
                                "product_code",
                                "currency",
                                "effective_from"
                        }
                )
        },
        indexes = {
                @Index(
                        name = "idx_interest_configs_lookup",
                        columnList = "product_code, currency, effective_from"
                )
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InterestConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(
            name = "product_code",
            nullable = false,
            length = 50
    )
    private String productCode;

    @Column(
            nullable = false,
            length = 3
    )
    private String currency;

    @Column(
            name = "interest_rate",
            nullable = false,
            precision = 10,
            scale = 6
    )
    private BigDecimal interestRate;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "day_count_convention",
            nullable = false,
            length = 30
    )
    private DayCountConvention dayCountConvention;

    @Column(
            name = "effective_from",
            nullable = false
    )
    private LocalDate effectiveFrom;

    @Column(name = "effective_to")
    private LocalDate effectiveTo;

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
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
}