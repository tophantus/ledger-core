package com.example.ledgercore.withdrawal.command.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WithdrawalCodeHasher {

    private final PasswordEncoder passwordEncoder;

    public String hash(String code) {
        return passwordEncoder.encode(code);
    }

    public boolean matches(
            String code,
            String hash
    ) {
        return passwordEncoder.matches(code, hash);
    }
}