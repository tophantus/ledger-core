package com.example.ledgercore.card.command.port.outbound;

import com.example.ledgercore.common.currency.Currency;

import java.math.BigDecimal;
import java.util.UUID;

public interface CreditAuthorizationHoldPort {

    CreditHoldResult createHold(
            UUID creditFacilityId,
            UUID authorizationId,
            String reference,
            BigDecimal amount,
            Currency currency
    );

    record CreditHoldResult(
            UUID holdId
    ) {
    }
}