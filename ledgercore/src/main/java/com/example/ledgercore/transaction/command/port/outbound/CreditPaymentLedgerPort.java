package com.example.ledgercore.transaction.command.port.outbound;

import com.example.ledgercore.common.currency.Currency;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public interface CreditPaymentLedgerPort {

    void recordCreditPayment(
            UUID transactionId,
            UUID creditFacilityId,
            UUID providerAccountId,
            BigDecimal amount,
            Currency currency,
            LocalDate businessDate
    );
}