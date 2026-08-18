package com.example.ledgercore.auth.command.dto;

import java.util.UUID;

public record VerifyEmailCommand(
        UUID userId,
        String otp
) {
}