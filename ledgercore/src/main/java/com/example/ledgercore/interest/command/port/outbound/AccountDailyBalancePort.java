package com.example.ledgercore.interest.command.port.outbound;

import java.time.LocalDate;
import java.util.UUID;

public interface AccountDailyBalancePort {

    AccountDailyBalanceInfo findClosingBalance(
            UUID accountId,
            LocalDate businessDate
    );
}