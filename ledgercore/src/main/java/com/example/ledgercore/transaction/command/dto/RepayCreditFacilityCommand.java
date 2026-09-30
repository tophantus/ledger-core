package com.example.ledgercore.transaction.command.dto;

import com.example.ledgercore.common.currency.Currency;

import java.math.BigDecimal;
import java.util.UUID;

public record RepayCreditFacilityCommand(
        UUID accountId,
        UUID creditFacilityId,
        BigDecimal amount,
        Currency currency,
        String reference,
        String description
) {
}