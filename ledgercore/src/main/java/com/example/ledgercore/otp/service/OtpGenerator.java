package com.example.ledgercore.otp.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
@RequiredArgsConstructor
public class OtpGenerator {

    private static final int OTP_LENGTH = 6;

    private final SecureRandom secureRandom;

    public String generate() {
        return String.format(
                "%0" + OTP_LENGTH + "d",
                secureRandom.nextInt(1_000_000)
        );
    }
}