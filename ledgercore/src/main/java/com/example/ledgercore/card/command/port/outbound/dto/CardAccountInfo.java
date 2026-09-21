package com.example.ledgercore.card.command.port.outbound.dto;

import com.example.ledgercore.common.currency.Currency;

import java.math.BigDecimal;
import java.util.UUID;

public record CardAccountInfo(
        UUID id,
        UUID userId,
        UUID productId,
        BigDecimal availableBalance,
        Currency currency
) {
}