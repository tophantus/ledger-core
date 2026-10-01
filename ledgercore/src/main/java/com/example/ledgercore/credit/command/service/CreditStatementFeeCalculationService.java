package com.example.ledgercore.credit.command.service;

import com.example.ledgercore.common.currency.Currency;
import com.example.ledgercore.credit.entity.CreditStatement;

import java.math.BigDecimal;

public interface CreditStatementFeeCalculationService {

    BigDecimal calculateOverdueFee(
            CreditStatement statement,
            Currency currency
    );
}