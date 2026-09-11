package com.example.ledgercore.interest.command.port.outbound;

import com.example.ledgercore.common.currency.Currency;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public interface InterestTransactionPort {

    UUID postInterest(
            UUID accountId,
            BigDecimal amount,
            Currency currency,
            LocalDate businessDate
    );
}
