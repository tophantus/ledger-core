package com.example.ledgercore.credit.command.port.outbound;

import com.example.ledgercore.common.currency.Currency;

import java.util.UUID;

public interface CreditLedgerAccountPort {

    UUID createCreditLedgerAccount(
            UUID creditFacilityId,
            Currency currency
    );
}