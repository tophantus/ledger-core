package com.example.ledgercore.credit.command.service.dto;

import com.example.ledgercore.common.currency.Currency;

import java.math.BigDecimal;
import java.util.UUID;

public record UpdateCreditFacilityCommand(
        UUID customerId,
        UUID facilityId,
        UUID productId,
        BigDecimal creditLimit,
        Currency currency
) {
}