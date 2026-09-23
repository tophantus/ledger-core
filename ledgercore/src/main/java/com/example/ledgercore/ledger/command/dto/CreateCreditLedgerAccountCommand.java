package com.example.ledgercore.ledger.command.dto;

import com.example.ledgercore.common.currency.Currency;

import java.util.UUID;

public record CreateCreditLedgerAccountCommand(
        UUID creditFacilityId,
        Currency currency
) {
}