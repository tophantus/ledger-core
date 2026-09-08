package com.example.ledgercore.notification.mail.service;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

@Component
public class MoneyFormatter {

    private static final Locale LOCALE = Locale.US;

    public String format(
            BigDecimal amount,
            String currency
    ) {
        if (amount == null) {
            throw new IllegalArgumentException(
                    "amount must not be null"
            );
        }

        if (currency == null || currency.isBlank()) {
            throw new IllegalArgumentException(
                    "currency must not be blank"
            );
        }

        String formattedAmount =
                NumberFormat
                        .getNumberInstance(LOCALE)
                        .format(amount.stripTrailingZeros());

        return formattedAmount + " " + currency;
    }
}