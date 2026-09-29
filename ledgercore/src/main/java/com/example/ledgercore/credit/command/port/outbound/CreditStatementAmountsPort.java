package com.example.ledgercore.credit.command.port.outbound;

import com.example.ledgercore.credit.command.port.outbound.dto.CreditStatementAmounts;

import java.time.LocalDate;
import java.util.UUID;

public interface CreditStatementAmountsPort {

    CreditStatementAmounts getAmounts(
            UUID creditFacilityId,
            LocalDate periodStart,
            LocalDate periodEnd
    );
}
