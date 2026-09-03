package com.example.ledgercore.businessday.query.dto;

import com.example.ledgercore.businessday.enums.BusinessDayStatus;

import java.time.Instant;
import java.time.LocalDate;

public record BusinessDayResponse(
        LocalDate businessDate,
        BusinessDayStatus status,
        Instant openedAt,
        Instant closedAt
) {
}