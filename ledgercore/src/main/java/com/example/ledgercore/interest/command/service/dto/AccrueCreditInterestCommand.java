package com.example.ledgercore.interest.command.service.dto;

import com.example.ledgercore.common.currency.Currency;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record AccrueCreditInterestCommand(
        UUID runId,
        UUID creditFacilityId,
        UUID productId,
        Currency currency,
        BigDecimal closingBalance,
        LocalDate businessDate
) {
}
