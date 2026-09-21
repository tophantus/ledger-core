package com.example.ledgercore.credit.command.port.inbound;

import com.example.ledgercore.common.currency.Currency;

import java.math.BigDecimal;
import java.util.UUID;

public interface IncreaseCreditFacilityHoldUseCase {

    void execute(
            UUID creditFacilityId,
            BigDecimal amount,
            Currency currency
    );
}