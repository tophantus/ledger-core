package com.example.ledgercore.account.command.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record WithdrawAccountCommand(
        UUID accountId,
        BigDecimal amount
) {
}