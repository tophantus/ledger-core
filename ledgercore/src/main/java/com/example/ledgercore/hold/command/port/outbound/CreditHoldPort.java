package com.example.ledgercore.hold.command.port.outbound;

import com.example.ledgercore.common.currency.Currency;

import java.math.BigDecimal;
import java.util.UUID;

public interface CreditHoldPort {

    void increaseHold(
            UUID creditFacilityId,
            BigDecimal amount,
            Currency currency
    );
}