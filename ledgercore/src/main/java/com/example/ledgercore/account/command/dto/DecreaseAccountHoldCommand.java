package com.example.ledgercore.account.command.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record DecreaseAccountHoldCommand(
        UUID accountId,
        BigDecimal amount,
        String currency
) {
}