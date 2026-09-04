package com.example.ledgercore.ledger.query.repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public interface JournalReconciliationProjection {

    UUID getId();

    UUID getTransactionId();

    BigDecimal getDebitTotal();

    BigDecimal getCreditTotal();

    LocalDate getBusinessDate();
}