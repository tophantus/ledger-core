package com.example.ledgercore.reconciliation.enums;

public enum ReconciliationErrorCode {

    // Transaction ↔ Journal

    JOURNAL_NOT_FOUND,

    TRANSACTION_AMOUNT_MISMATCH,

    BUSINESS_DATE_MISMATCH,


    // Journal ↔ Journal Lines

    JOURNAL_NOT_BALANCED,


    // Account ↔ Daily Balance

    BALANCE_MISMATCH,

    OPENING_BALANCE_MISMATCH
}