package com.example.ledgercore.transaction.query.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record GetCreditFeeAmountResult(
        UUID creditFacilityId,
        LocalDate fromDate,
        LocalDate toDate,
        BigDecimal totalAmount
) {
}