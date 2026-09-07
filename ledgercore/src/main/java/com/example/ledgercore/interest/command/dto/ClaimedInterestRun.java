package com.example.ledgercore.interest.command.dto;

import java.time.LocalDate;
import java.util.UUID;

public record ClaimedInterestRun(
        UUID runId,
        LocalDate businessDate,
        UUID lastProcessedId,
        long processedCount
) {
}