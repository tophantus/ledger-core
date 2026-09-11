package com.example.ledgercore.account.query.dto;

import com.example.ledgercore.common.currency.Currency;

import java.math.BigDecimal;
import java.util.UUID;

public record AccountTransferInfo(
        UUID sourceAccountId,
        UUID destinationAccountId,
        Currency currency,
        BigDecimal sourceAvailableBalance
) {
}