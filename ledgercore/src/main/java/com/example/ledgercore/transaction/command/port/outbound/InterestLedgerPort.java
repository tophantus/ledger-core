package com.example.ledgercore.transaction.command.port.outbound;

import com.example.ledgercore.common.currency.Currency;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public interface InterestLedgerPort {

    void recordInterestPosting(
            UUID transactionId,
            UUID accountId,
            BigDecimal amount,
            Currency currency,
            LocalDate businessDate
    );
}