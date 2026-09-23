package com.example.ledgercore.credit.command.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public interface CreditDailyBalanceService {

    void updateClosingBalance(
            UUID creditFacilityId,
            LocalDate businessDate,
            BigDecimal closingBalance
    );
}