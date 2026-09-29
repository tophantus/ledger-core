package com.example.ledgercore.credit.command.dto;

import java.time.LocalDate;
import java.util.UUID;

public record CreateCreditStatementCommand(
        UUID creditFacilityId,
        LocalDate periodStart,
        LocalDate periodEnd,
        LocalDate statementDate,
        LocalDate dueDate
) {
}
