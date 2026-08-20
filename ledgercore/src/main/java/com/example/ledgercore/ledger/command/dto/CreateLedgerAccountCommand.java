package com.example.ledgercore.ledger.command.dto;

public record CreateLedgerAccountCommand(
        String accountNo,
        String currency
) {
}