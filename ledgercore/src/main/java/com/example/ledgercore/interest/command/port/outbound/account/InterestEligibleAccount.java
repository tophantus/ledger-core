package com.example.ledgercore.interest.command.port.outbound.account;

import com.example.ledgercore.common.currency.Currency;

import java.math.BigDecimal;
import java.util.UUID;

public record InterestEligibleAccount(
        UUID accountId,
        UUID productId,
        Currency currency,
        BigDecimal closingBalance
) {
}