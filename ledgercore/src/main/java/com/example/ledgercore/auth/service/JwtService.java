package com.example.ledgercore.auth.service;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public interface JwtService {

    String generateAccessToken(
            UUID userId,
            String username,
            Set<String> roles
    );

    boolean validateAccessToken(String token);

    UUID extractUserId(String token);

    String extractUsername(String token);

    Set<String> extractRoles(String token);

    Instant extractExpiration(String token);
}