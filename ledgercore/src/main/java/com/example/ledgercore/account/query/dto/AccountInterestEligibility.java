package com.example.ledgercore.account.query.dto;

import java.util.UUID;

public record AccountInterestEligibility(
        UUID accountId,
        UUID productId,
        String currency
) {
}