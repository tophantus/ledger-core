package com.example.ledgercore.credit.command.dto;

import com.example.ledgercore.common.currency.Currency;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record IncreaseCreditFacilityOutstandingBalanceCommand(
        UUID creditFacilityId,
        BigDecimal amount,
        Currency currency,
        LocalDate businessDate
) {
}