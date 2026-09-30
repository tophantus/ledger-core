package com.example.ledgercore.credit.command.service.dto;

import java.time.LocalDate;
import java.util.UUID;

public record CreateCreditStatementCommand(
        UUID runId,
        UUID creditFacilityId,
        LocalDate periodStart,
        LocalDate periodEnd,
        LocalDate statementDate,
        LocalDate dueDate
) {
}
