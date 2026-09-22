package com.example.ledgercore.provider.command.service.impl;

import com.example.ledgercore.provider.command.service.PaymentProviderCredentialGenerator;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.Base64;

@Component
public class SecurePaymentProviderCredentialGenerator
        implements PaymentProviderCredentialGenerator {

    private static final int CREDENTIAL_BYTES = 32;

    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    public String generate() {
        byte[] bytes = new byte[CREDENTIAL_BYTES];

        secureRandom.nextBytes(bytes);

        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(bytes);
    }
}