package com.example.ledgercore.account.command.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record IncreaseAccountHoldCommand(
        UUID accountId,
        BigDecimal amount,
        String currency
) {
}