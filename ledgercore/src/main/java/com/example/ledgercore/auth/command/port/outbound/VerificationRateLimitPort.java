package com.example.ledgercore.auth.command.port.outbound;

import java.util.UUID;

public interface VerificationRateLimitPort {

    RateLimitResult checkAndRecord(UUID userId);

    enum RateLimitResult {
        ALLOWED,
        COOLDOWN,
        LIMIT_EXCEEDED
    }
}