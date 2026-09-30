package com.example.ledgercore.credit.command.dto.run;

import java.time.LocalDate;
import java.util.UUID;

public record ClaimedCreditStatementRun(
        UUID runId,
        LocalDate periodStart,
        LocalDate periodEnd,
        LocalDate statementDate,
        LocalDate dueDate,
        UUID lastProcessedFacilityId
) {
}