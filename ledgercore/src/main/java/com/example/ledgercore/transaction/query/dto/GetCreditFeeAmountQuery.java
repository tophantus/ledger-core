package com.example.ledgercore.transaction.query.dto;

import java.time.LocalDate;
import java.util.UUID;

public record GetCreditFeeAmountQuery(
        UUID creditFacilityId,
        LocalDate fromDate,
        LocalDate toDate
) {
}