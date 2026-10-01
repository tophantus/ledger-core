package com.example.ledgercore.credit.command.port.outbound;

import com.example.ledgercore.common.currency.Currency;

import java.math.BigDecimal;
import java.util.UUID;

public interface CreditFacilityFeePort {

    void createFee(
            UUID creditFacilityId,
            BigDecimal amount,
            Currency currency,
            String reference,
            String description
    );
}