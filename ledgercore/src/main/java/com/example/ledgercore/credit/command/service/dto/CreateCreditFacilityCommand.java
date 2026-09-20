package com.example.ledgercore.credit.command.service.dto;

import com.example.ledgercore.common.currency.Currency;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateCreditFacilityCommand(
        UUID customerId,
        UUID productId,
        BigDecimal creditLimit,
        Currency currency
) {
}