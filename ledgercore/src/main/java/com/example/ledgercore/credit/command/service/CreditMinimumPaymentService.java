package com.example.ledgercore.credit.command.service;

import com.example.ledgercore.common.currency.Currency;

import java.math.BigDecimal;

public interface CreditMinimumPaymentService {

    BigDecimal calculate(
            BigDecimal closingBalance,
            Currency currency
    );
}