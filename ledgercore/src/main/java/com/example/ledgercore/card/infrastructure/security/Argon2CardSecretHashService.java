package com.example.ledgercore.card.infrastructure.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class Argon2CardSecretHashService
        implements CardSecretHashService {

    private final PasswordEncoder passwordEncoder;

    @Override
    public String hash(String secret) {
        return passwordEncoder.encode(secret);
    }

    @Override
    public boolean matches(String secret, String hash) {
        return passwordEncoder.matches(secret, hash);
    }
}