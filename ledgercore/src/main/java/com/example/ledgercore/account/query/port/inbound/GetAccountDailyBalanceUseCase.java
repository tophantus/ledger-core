package com.example.ledgercore.account.query.port.inbound;

import com.example.ledgercore.account.query.dto.AccountDailyBalanceResponse;

import java.time.LocalDate;
import java.util.UUID;

public interface GetAccountDailyBalanceUseCase {

    AccountDailyBalanceResponse execute(
            UUID accountId,
            LocalDate businessDate
    );
}