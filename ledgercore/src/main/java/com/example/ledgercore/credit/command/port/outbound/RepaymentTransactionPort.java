package com.example.ledgercore.credit.command.port.outbound;

import com.example.ledgercore.common.currency.Currency;

import java.math.BigDecimal;
import java.util.UUID;

public interface RepaymentTransactionPort {

    void repay(
            UUID accountId,
            UUID creditFacilityId,
            BigDecimal amount,
            Currency currency,
            String reference,
            String description
    );
}