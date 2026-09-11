package com.example.ledgercore.interest.command.dto;

import com.example.ledgercore.interest.enums.InterestRunType;

import java.time.LocalDate;
import java.util.UUID;

public record ClaimedInterestRun(
        UUID runId,
        LocalDate businessDate,
        InterestRunType runType,
        UUID lastProcessedId,
        long processedCount
) {
}