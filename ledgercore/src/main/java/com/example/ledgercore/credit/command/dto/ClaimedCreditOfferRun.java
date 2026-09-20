package com.example.ledgercore.credit.command.dto;

import java.time.LocalDate;
import java.util.UUID;

public record ClaimedCreditOfferRun(
        UUID runId,
        LocalDate businessDate,
        UUID lastProcessedId
) {
}