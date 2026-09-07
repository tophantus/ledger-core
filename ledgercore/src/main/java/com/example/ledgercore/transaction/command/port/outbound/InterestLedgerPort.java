package com.example.ledgercore.transaction.command.port.outbound;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public interface InterestLedgerPort {

    void recordInterestPosting(
            UUID transactionId,
            UUID accountId,
            BigDecimal amount,
            String currency,
            LocalDate businessDate
    );
}