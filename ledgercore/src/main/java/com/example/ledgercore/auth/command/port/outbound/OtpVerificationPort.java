package com.example.ledgercore.auth.command.port.outbound;

import java.util.UUID;

public interface OtpVerificationPort {

    void sendSignupOtp(
            UUID userId,
            String destination
    );

    void verifySignupOtp(
            UUID userId,
            String otp
    );
}