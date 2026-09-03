package com.example.ledgercore.businessday.entity;

import com.example.ledgercore.businessday.enums.BusinessDayStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "business_days")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BusinessDay {

    @Id
    @Column(name = "business_date", nullable = false)
    private LocalDate businessDate;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "status",
            nullable = false,
            length = 20
    )
    private BusinessDayStatus status;

    @Column(
            name = "opened_at",
            nullable = false
    )
    private Instant openedAt;

    @Column(name = "closed_at")
    private Instant closedAt;
}