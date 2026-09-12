package com.example.ledgercore.interest.query.dto;

import java.time.LocalDate;
import java.util.UUID;

public record GetAdminInterestAccrualsQuery(
        UUID runId,
        LocalDate businessDate,
        UUID accountId,
        int page,
        int size
) {
}