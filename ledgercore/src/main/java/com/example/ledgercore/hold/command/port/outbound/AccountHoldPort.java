package com.example.ledgercore.hold.command.port.outbound;

import com.example.ledgercore.common.currency.Currency;

import java.math.BigDecimal;
import java.util.UUID;

public interface AccountHoldPort {

    void increaseHold(
            UUID accountId,
            BigDecimal amount,
            Currency currency
    );

    void decreaseHold(
            UUID accountId,
            BigDecimal amount,
            Currency currency
    );
}