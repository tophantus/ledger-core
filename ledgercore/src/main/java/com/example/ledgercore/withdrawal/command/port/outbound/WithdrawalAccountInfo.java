package com.example.ledgercore.withdrawal.command.port.outbound;

import java.math.BigDecimal;
import java.util.UUID;

public record WithdrawalAccountInfo(
        UUID accountId,
        UUID userId,
        String currency,
        BigDecimal balance
) {
}
