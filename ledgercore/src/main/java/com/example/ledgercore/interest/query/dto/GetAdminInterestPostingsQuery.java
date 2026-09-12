package com.example.ledgercore.interest.query.dto;

import java.time.LocalDate;
import java.util.UUID;

public record GetAdminInterestPostingsQuery(
        UUID runId,
        LocalDate businessDate,
        UUID accountId,
        int page,
        int size
) {
}