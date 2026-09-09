package com.example.ledgercore.atm.command.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AtmCredentialHasher {

    private final PasswordEncoder passwordEncoder;

    public String hash(String credential) {
        return passwordEncoder.encode(credential);
    }

    public boolean matches(
            String credential,
            String hash
    ) {
        return passwordEncoder.matches(
                credential,
                hash
        );
    }
}