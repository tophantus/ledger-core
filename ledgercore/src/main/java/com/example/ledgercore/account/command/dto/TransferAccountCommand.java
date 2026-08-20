package com.example.ledgercore.account.command.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record TransferAccountCommand(
        UUID sourceAccountId,
        UUID destinationAccountId,
        BigDecimal amount
) {
}