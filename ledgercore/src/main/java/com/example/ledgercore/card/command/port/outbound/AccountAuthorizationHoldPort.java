package com.example.ledgercore.card.command.port.outbound;

import com.example.ledgercore.common.currency.Currency;

import java.math.BigDecimal;
import java.util.UUID;

public interface AccountAuthorizationHoldPort {

    HoldResult createHold(
            UUID accountId,
            UUID authorizationId,
            BigDecimal amount,
            Currency currency
    );

    record HoldResult(
            UUID holdId
    ) {
    }
}