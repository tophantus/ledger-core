package com.example.ledgercore.user.query.dto;

import java.util.Set;
import java.util.UUID;

public record UserAuthenticationResponse(
        UUID userId,
        String username,
        String email,
        String passwordHash,
        Set<String> roles,
        boolean active
) {
}