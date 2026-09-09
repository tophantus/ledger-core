package com.example.ledgercore.hold.command.port.outbound;

import java.math.BigDecimal;
import java.util.UUID;

public interface AccountHoldPort {

    void increaseHold(
            UUID accountId,
            BigDecimal amount,
            String currency
    );

    void decreaseHold(
            UUID accountId,
            BigDecimal amount,
            String currency
    );
}