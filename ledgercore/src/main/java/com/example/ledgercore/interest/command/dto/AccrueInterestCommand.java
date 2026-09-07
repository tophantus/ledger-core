package com.example.ledgercore.interest.command.dto;

import java.time.LocalDate;
import java.util.UUID;

public record AccrueInterestCommand(
        UUID runId,
        UUID accountId,
        UUID productId,
        String currency,
        LocalDate businessDate
) {
}