package com.example.ledgercore.ledger.command.dto;

import com.example.ledgercore.common.currency.Currency;

public record CreateLedgerAccountCommand(
        String accountNo,
        Currency currency
) {
}