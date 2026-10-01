package com.example.ledgercore.credit.query.dto;

import java.util.UUID;

public record GetCreditStatementsQuery(
        UUID customerId,
        UUID creditFacilityId,
        int page,
        int size
) {
}