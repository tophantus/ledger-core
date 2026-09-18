package com.example.ledgercore.card.infrastructure.generator;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
public class SecureCardSecurityCodeGenerator
        implements CardSecurityCodeGenerator {

    private static final int CVV_LENGTH = 3;

    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    public String generate() {
        int upperBound = 1_000;

        return String.format(
                "%0" + CVV_LENGTH + "d",
                secureRandom.nextInt(upperBound)
        );
    }
}