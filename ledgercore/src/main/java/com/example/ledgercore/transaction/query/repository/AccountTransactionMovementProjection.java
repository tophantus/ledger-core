package com.example.ledgercore.transaction.query.repository;

import java.math.BigDecimal;
import java.util.UUID;

public interface AccountTransactionMovementProjection {

    UUID getAccountId();

    BigDecimal getTotalCredit();

    BigDecimal getTotalDebit();
}