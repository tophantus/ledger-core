package com.example.ledgercore.account.query.repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public interface AccountDailyBalanceReconciliationProjection {

    UUID getAccountId();

    LocalDate getBusinessDate();

    BigDecimal getOpeningBalance();

    BigDecimal getClosingBalance();
}