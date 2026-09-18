package com.example.ledgercore.card.infrastructure.generator;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
public class SecureCardNumberGenerator
        implements CardNumberGenerator {

    private static final int PAN_LENGTH = 16;

    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    public String generate() {
        StringBuilder pan = new StringBuilder(PAN_LENGTH);

        for (int i = 0; i < PAN_LENGTH - 1; i++) {
            pan.append(secureRandom.nextInt(10));
        }

        pan.append(calculateLuhnCheckDigit(pan.toString()));

        return pan.toString();
    }

    private int calculateLuhnCheckDigit(String number) {
        int sum = 0;
        boolean doubleDigit = true;

        for (int i = number.length() - 1; i >= 0; i--) {
            int digit = number.charAt(i) - '0';

            if (doubleDigit) {
                digit *= 2;

                if (digit > 9) {
                    digit -= 9;
                }
            }

            sum += digit;
            doubleDigit = !doubleDigit;
        }

        return (10 - (sum % 10)) % 10;
    }
}