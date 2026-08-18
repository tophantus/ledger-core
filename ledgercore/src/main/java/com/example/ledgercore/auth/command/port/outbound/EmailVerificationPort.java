package com.example.ledgercore.auth.command.port.outbound;

import java.util.UUID;

public interface EmailVerificationPort {

    void sendVerificationCode(
            UUID userId,
            String destination
    );

    VerificationResult verifyEmail(
            UUID userId,
            String code
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