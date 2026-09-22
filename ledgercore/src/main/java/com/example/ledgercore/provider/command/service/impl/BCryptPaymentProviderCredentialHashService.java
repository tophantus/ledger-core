package com.example.ledgercore.provider.command.service.impl;

import com.example.ledgercore.provider.command.service.PaymentProviderCredentialHashService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BCryptPaymentProviderCredentialHashService
        implements PaymentProviderCredentialHashService {

    private final PasswordEncoder passwordEncoder;

    @Override
    public String hash(String credential) {
        return passwordEncoder.encode(credential);
    }

    @Override
    public boolean matches(
            String credential,
            String credentialHash
    ) {
        return passwordEncoder.matches(
                credential,
                credentialHash
        );
    }
}