package com.example.ledgercore.withdrawal.command.service;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
public class WithdrawalCodeGenerator {

    private final SecureRandom secureRandom = new SecureRandom();

    public String generate() {
        return String.format(
                "%06d",
                secureRandom.nextInt(1_000_000)
        );
    }
}