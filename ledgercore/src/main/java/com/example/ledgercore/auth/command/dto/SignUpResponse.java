package com.example.ledgercore.auth.command.dto;

import java.util.UUID;

public record SignUpResponse(
        UUID userId,
        String username,
        String email,
        boolean active,
        String message
) {
}