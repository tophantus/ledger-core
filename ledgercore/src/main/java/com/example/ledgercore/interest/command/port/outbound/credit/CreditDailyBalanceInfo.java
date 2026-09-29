package com.example.ledgercore.interest.command.port.outbound.credit;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record CreditDailyBalanceInfo(
        UUID creditFacilityId,
        LocalDate businessDate,
        BigDecimal closingBalance
) {
}
