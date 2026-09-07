package com.example.ledgercore.account.query.port.inbound;

import com.example.ledgercore.account.query.dto.AccountInterestEligibility;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface GetAccountsEligibleForInterestUseCase {

    List<AccountInterestEligibility> execute(
            LocalDate businessDate,
            UUID lastProcessedId,
            int batchSize
    );
}