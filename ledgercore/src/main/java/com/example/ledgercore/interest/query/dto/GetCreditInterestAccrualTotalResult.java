package com.example.ledgercore.interest.query.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record GetCreditInterestAccrualTotalResult(
        UUID creditFacilityId,
        LocalDate fromDate,
        LocalDate toDate,
        BigDecimal totalInterestAmount
) {
}