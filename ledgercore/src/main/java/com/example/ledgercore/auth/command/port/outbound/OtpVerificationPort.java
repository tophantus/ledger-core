package com.example.ledgercore.auth.command.port.outbound;

import java.util.UUID;

public interface OtpVerificationPort {

    void sendSignupOtp(
            UUID userId,
            String destination
    );

    VerificationResult verifySignupOtp(
            UUID userId,
            String otp
    );

    record VerificationResult(
            Status status
    ) {
    }

    enum Status {
        VERIFIED,
        INVALID,
        EXPIRED,
        ALREADY_VERIFIED
    }
}