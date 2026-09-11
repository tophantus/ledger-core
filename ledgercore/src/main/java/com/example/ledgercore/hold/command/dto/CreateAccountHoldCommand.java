package com.example.ledgercore.hold.command.dto;

import com.example.ledgercore.common.currency.Currency;
import com.example.ledgercore.hold.enums.AccountHoldReferenceType;
import com.example.ledgercore.hold.enums.AccountHoldType;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateAccountHoldCommand(
        UUID accountId,
        BigDecimal amount,
        Currency currency,
        AccountHoldType holdType,
        AccountHoldReferenceType referenceType,
        UUID referenceId
) {
}