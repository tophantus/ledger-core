package com.example.ledgercore.withdrawal.command.port.outbound;

import com.example.ledgercore.common.currency.Currency;

import java.math.BigDecimal;
import java.util.UUID;

public record WithdrawalAccountInfo(
        UUID accountId,
        UUID userId,
        Currency currency,
        BigDecimal availableBalance
) {
}
