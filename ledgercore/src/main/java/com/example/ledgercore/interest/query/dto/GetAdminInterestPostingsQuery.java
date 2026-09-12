package com.example.ledgercore.interest.query.dto;

import com.example.ledgercore.common.currency.Currency;

import java.time.LocalDate;
import java.util.UUID;

public record GetAdminInterestPostingsQuery(
        UUID runId,
        LocalDate businessDate,
        UUID accountId,
        Currency currency,
        int page,
        int size
) {
}