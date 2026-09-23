package com.example.ledgercore.transaction.command.dto;

import com.example.ledgercore.common.currency.Currency;

import java.math.BigDecimal;
import java.util.UUID;

public record TransferCreditFacilityToProviderCommand(
        UUID creditFacilityId,
        UUID providerAccountId,
        BigDecimal amount,
        Currency currency,
        String reference,
        String description
) {
}