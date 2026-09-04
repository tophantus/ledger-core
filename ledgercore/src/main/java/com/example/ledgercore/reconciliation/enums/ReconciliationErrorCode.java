package com.example.ledgercore.reconciliation.enums;

public enum ReconciliationErrorCode {

    // Transaction ↔ Journal
    JOURNAL_NOT_FOUND,
    BUSINESS_DATE_MISMATCH,

    // Journal balance
    JOURNAL_NOT_BALANCED,

    // Account balance
    BALANCE_MISMATCH,
    OPENING_BALANCE_MISMATCH,

    // Account ↔ Ledger
    LEDGER_BALANCE_MISMATCH
}