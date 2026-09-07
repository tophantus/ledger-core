package com.example.ledgercore.interest.command.dto;

import java.time.LocalDate;
import java.util.UUID;

public record PostInterestCommand(
        UUID accountId,
        LocalDate periodStart,
        LocalDate periodEnd
) {
}
