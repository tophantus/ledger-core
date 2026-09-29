package com.example.ledgercore.credit.command.service.impl;

import com.example.ledgercore.common.currency.Currency;
import com.example.ledgercore.common.currency.CurrencyAmountPolicy;
import com.example.ledgercore.credit.command.service.CreditMinimumPaymentService;
import com.example.ledgercore.credit.config.CreditStatementProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class CreditMinimumPaymentServiceImpl
        implements CreditMinimumPaymentService {

    private final CreditStatementProperties properties;

    @Override
    public BigDecimal calculate(
            BigDecimal closingBalance,
            Currency currency
    ) {
        validate(
                closingBalance,
                currency
        );

        if (closingBalance.signum() == 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal rate = properties
                .getMinimumPayment()
                .getRate();

        BigDecimal fixedMinimum = CurrencyAmountPolicy.round(
                properties
                        .getMinimumPayment()
                        .getFixedMinimum(),
                currency
        );

        BigDecimal percentageAmount = closingBalance
                .multiply(rate);

        BigDecimal minimumPayment = percentageAmount.max(
                fixedMinimum
        );

        minimumPayment = minimumPayment.min(
                closingBalance
        );

        return CurrencyAmountPolicy.round(
                minimumPayment,
                currency
        );
    }

    private void validate(
            BigDecimal closingBalance,
            Currency currency
    ) {
        if (closingBalance == null) {
            throw new IllegalArgumentException(
                    "closingBalance must not be null"
            );
        }

        if (currency == null) {
            throw new IllegalArgumentException(
                    "currency must not be null"
            );
        }

        if (closingBalance.signum() < 0) {
            throw new IllegalArgumentException(
                    "closingBalance must not be negative"
            );
        }

        CurrencyAmountPolicy.validate(
                closingBalance,
                currency
        );
    }
}