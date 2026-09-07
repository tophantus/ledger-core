package com.example.ledgercore.interest.command.port.outbound;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public interface InterestJournalPort {

    UUID recordAccrualJournal(
            UUID accrualId,
            LocalDate businessDate,
            String currency,
            BigDecimal amount
    );
}