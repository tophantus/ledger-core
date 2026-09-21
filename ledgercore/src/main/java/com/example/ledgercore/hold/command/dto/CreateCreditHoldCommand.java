package com.example.ledgercore.hold.command.dto;

import com.example.ledgercore.common.currency.Currency;
import com.example.ledgercore.hold.enums.CreditHoldReferenceType;
import com.example.ledgercore.hold.enums.CreditHoldType;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateCreditHoldCommand(
        UUID creditFacilityId,
        BigDecimal amount,
        Currency currency,
        CreditHoldType holdType,
        CreditHoldReferenceType referenceType,
        UUID referenceId
) {
}