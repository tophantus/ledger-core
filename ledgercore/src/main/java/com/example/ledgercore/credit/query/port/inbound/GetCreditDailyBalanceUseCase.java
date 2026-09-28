package com.example.ledgercore.credit.query.port.inbound;

import com.example.ledgercore.credit.query.dto.CreditDailyBalanceResponse;

import java.time.LocalDate;
import java.util.UUID;

public interface GetCreditDailyBalanceUseCase {

    CreditDailyBalanceResponse execute(
            UUID creditFacilityId,
            LocalDate businessDate
    );
}
