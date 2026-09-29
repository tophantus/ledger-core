package com.example.ledgercore.interest.query.dto;

import java.time.LocalDate;
import java.util.UUID;

public record GetCreditInterestAccrualTotalQuery(
        UUID creditFacilityId,
        LocalDate fromDate,
        LocalDate toDate
) {
}