package com.example.ledgercore.auth.command.dto;

import java.util.UUID;

public record VerifyOtpCommand(
        UUID userId,
        String otp
) {
}