package com.example.ledgercore.ledger.command.dto;

import com.example.ledgercore.common.currency.Currency;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record RecordCreditPaymentCommand(
        UUID transactionId,
        UUID creditFacilityId,
        UUID providerAccountId,
        BigDecimal amount,
        Currency currency,
        LocalDate businessDate
) {
}