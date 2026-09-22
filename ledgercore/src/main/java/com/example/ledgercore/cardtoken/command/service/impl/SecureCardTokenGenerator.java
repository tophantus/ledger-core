package com.example.ledgercore.cardtoken.command.service.impl;

import com.example.ledgercore.cardtoken.command.service.CardTokenGenerator;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.Base64;

@Component
public class SecureCardTokenGenerator
        implements CardTokenGenerator {

    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    public String generate() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);

        return "ct_"
                + Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(bytes);
    }
}