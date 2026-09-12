package com.example.ledgercore.common.currency;

import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.common.exception.ErrorCode;

import java.math.BigDecimal;

public final class CurrencyAmountPolicy {

    private CurrencyAmountPolicy() {
    }

    public static void validate(
            BigDecimal amount,
            Currency currency
    ) {
        validateArguments(
                amount,
                currency
        );

        BigDecimal normalizedAmount =
                amount.stripTrailingZeros();

        if (normalizedAmount.scale() > currency.scale()) {
            throw new BusinessException(
                    ErrorCode.INVALID_CURRENCY_AMOUNT
            );
        }
    }

    public static BigDecimal round(
            BigDecimal amount,
            Currency currency
    ) {
        validateArguments(
                amount,
                currency
        );

        return amount.setScale(
                currency.scale(),
                currency.roundingMode()
        );
    }

    private static void validateArguments(
            BigDecimal amount,
            Currency currency
    ) {
        if (amount == null) {
            throw new IllegalArgumentException(
                    "Amount must not be null"
            );
        }

        if (currency == null) {
            throw new IllegalArgumentException(
                    "Currency must not be null"
            );
        }
    }
}