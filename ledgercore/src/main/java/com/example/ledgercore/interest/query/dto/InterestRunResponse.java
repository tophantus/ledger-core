package com.example.ledgercore.interest.query.dto;

import com.example.ledgercore.interest.enums.InterestRunStatus;
import com.example.ledgercore.interest.enums.InterestRunType;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record InterestRunResponse(
        UUID id,
        LocalDate businessDate,
        InterestRunType runType,
        InterestRunStatus status,
        long processedCount,
        Instant startedAt,
        Instant completedAt,
        Instant createdAt
) {
}