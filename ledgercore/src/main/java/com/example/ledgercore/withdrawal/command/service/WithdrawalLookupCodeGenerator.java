package com.example.ledgercore.withdrawal.command.service;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
public class WithdrawalLookupCodeGenerator {

    private static final int CODE_BOUND = 100_000_000;

    private final SecureRandom secureRandom =
            new SecureRandom();

    public String generate() {
        return String.format(
                "%08d",
                secureRandom.nextInt(CODE_BOUND)
        );
    }
}