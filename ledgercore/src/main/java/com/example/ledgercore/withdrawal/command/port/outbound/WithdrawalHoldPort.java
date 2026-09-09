package com.example.ledgercore.withdrawal.command.port.outbound;

import java.math.BigDecimal;
import java.util.UUID;

public interface WithdrawalHoldPort {

    UUID createHold(
            UUID withdrawalIntentId,
            UUID accountId,
            BigDecimal amount,
            String currency
    );

    void releaseHold(UUID holdId);
}