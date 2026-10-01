package com.example.ledgercore.credit.command.port.outbound;

import com.example.ledgercore.common.currency.Currency;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public interface CreditInterestTransactionPort {

    void postInterest(
            UUID creditFacilityId,
            BigDecimal amount,
            Currency currency,
            LocalDate businessDate
    );
}