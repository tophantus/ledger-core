package com.example.ledgercore.interest.query.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record InterestPostingResponse(
        UUID id,
        UUID accountId,
        UUID runId,
        LocalDate periodStart,
        LocalDate periodEnd,
        BigDecimal interestAmount,
        UUID transactionId,
        Instant postedAt,
        Instant createdAt
) {
}