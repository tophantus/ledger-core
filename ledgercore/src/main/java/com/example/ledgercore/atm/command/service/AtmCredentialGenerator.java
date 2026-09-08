package com.example.ledgercore.atm.command.service;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
public class AtmCredentialGenerator {

    private static final int CREDENTIAL_LENGTH = 32;

    private static final char[] CHARACTERS =
            (
                    "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
                            + "abcdefghijklmnopqrstuvwxyz"
                            + "0123456789"
            ).toCharArray();

    private final SecureRandom secureRandom =
            new SecureRandom();

    public String generate() {
        StringBuilder credential =
                new StringBuilder(CREDENTIAL_LENGTH);

        for (int i = 0; i < CREDENTIAL_LENGTH; i++) {
            credential.append(
                    CHARACTERS[
                            secureRandom.nextInt(
                                    CHARACTERS.length
                            )
                            ]
            );
        }

        return credential.toString();
    }
}