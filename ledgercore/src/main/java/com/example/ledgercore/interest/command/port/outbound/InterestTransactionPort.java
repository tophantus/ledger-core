package com.example.ledgercore.interest.command.port.outbound;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public interface InterestTransactionPort {

    UUID postInterest(
            UUID accountId,
            BigDecimal amount,
            String currency,
            LocalDate businessDate
    );
}
