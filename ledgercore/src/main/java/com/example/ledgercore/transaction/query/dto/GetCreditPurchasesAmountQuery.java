package com.example.ledgercore.transaction.query.dto;

import java.time.LocalDate;
import java.util.UUID;

public record GetCreditPurchasesAmountQuery(
        UUID creditFacilityId,
        LocalDate fromDate,
        LocalDate toDate
) {
}