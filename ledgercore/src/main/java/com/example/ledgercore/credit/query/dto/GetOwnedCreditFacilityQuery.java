package com.example.ledgercore.credit.query.dto;

import java.util.UUID;

public record GetOwnedCreditFacilityQuery(
        UUID customerId,
        UUID creditFacilityId
) {
}