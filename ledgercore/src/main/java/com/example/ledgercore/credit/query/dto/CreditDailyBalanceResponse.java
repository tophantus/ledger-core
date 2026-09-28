package com.example.ledgercore.credit.query.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record CreditDailyBalanceResponse(
        UUID creditFacilityId,
        LocalDate businessDate,
        BigDecimal closingBalance
) {
}
