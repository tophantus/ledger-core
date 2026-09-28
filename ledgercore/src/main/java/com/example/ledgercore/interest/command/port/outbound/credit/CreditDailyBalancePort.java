package com.example.ledgercore.interest.command.port.outbound.credit;

import java.time.LocalDate;
import java.util.UUID;

public interface CreditDailyBalancePort {

    CreditDailyBalanceInfo findClosingBalance(
            UUID creditFacilityId,
            LocalDate businessDate
    );
}
