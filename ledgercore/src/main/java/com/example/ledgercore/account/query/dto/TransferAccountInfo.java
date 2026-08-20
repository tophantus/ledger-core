package com.example.ledgercore.account.query.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record TransferAccountInfo(
        UUID sourceAccountId,
        UUID destinationAccountId,
        String currency,
        BigDecimal sourceBalance
) {
}