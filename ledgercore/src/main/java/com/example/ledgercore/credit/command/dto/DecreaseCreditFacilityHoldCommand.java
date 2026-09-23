package com.example.ledgercore.credit.command.dto;

import com.example.ledgercore.common.currency.Currency;

import java.math.BigDecimal;
import java.util.UUID;

public record DecreaseCreditFacilityHoldCommand(
        UUID creditFacilityId,
        BigDecimal amount,
        Currency currency
) {
}
