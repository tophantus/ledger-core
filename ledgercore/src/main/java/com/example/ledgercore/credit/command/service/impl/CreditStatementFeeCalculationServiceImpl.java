package com.example.ledgercore.credit.command.service.impl;

import com.example.ledgercore.common.currency.Currency;
import com.example.ledgercore.common.currency.CurrencyAmountPolicy;
import com.example.ledgercore.credit.command.service.CreditStatementFeeCalculationService;
import com.example.ledgercore.credit.config.CreditFeeProperties;
import com.example.ledgercore.credit.entity.CreditStatement;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class CreditStatementFeeCalculationServiceImpl
        implements CreditStatementFeeCalculationService {

    private final CreditFeeProperties creditFeeProperties;

    @Override
    public BigDecimal calculateOverdueFee(
            CreditStatement statement,
            Currency currency
    ) {
        if (statement == null) {
            throw new IllegalArgumentException(
                    "statement must not be null"
            );
        }

        if (statement.isMinimumPaymentSatisfied()) {
            return BigDecimal.ZERO;
        }

        BigDecimal fee = creditFeeProperties
                .getOverdueFees()
                .get(currency);

        if (fee == null) {
            throw new IllegalStateException(
                    "Overdue fee is not configured for currency: "
                            + currency
            );
        }

        return CurrencyAmountPolicy.round(
                fee,
                currency
        );
    }
}