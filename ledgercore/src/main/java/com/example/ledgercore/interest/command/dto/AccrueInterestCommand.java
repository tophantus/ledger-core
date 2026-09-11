package com.example.ledgercore.interest.command.dto;

import com.example.ledgercore.common.currency.Currency;

import java.time.LocalDate;
import java.util.UUID;

public record AccrueInterestCommand(
        UUID runId,
        UUID accountId,
        UUID productId,
        Currency currency,
        LocalDate businessDate
) {
}