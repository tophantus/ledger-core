package com.example.ledgercore.webhook.service;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.Base64;

@Component
public class WebhookSecretGenerator {

    private static final String PREFIX = "whsec_";
    private static final int SECRET_LENGTH = 32;

    private final SecureRandom secureRandom = new SecureRandom();

    public String generate() {
        byte[] bytes = new byte[SECRET_LENGTH];
        secureRandom.nextBytes(bytes);

        return PREFIX + Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(bytes);
    }
}