package com.example.ledgercore.interest.query.dto;

import com.example.ledgercore.common.currency.Currency;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record InterestAccrualResponse(
        UUID id,
        UUID accountId,
        Currency currency,
        LocalDate businessDate,
        UUID interestConfigId,
        BigDecimal principalAmount,
        BigDecimal interestRate,
        BigDecimal interestAmount,
        UUID journalEntryId,
        UUID postingId,
        UUID runId,
        Instant createdAt
) {
}