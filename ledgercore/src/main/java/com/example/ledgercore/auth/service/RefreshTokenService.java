package com.example.ledgercore.auth.service;

import java.util.UUID;

public interface RefreshTokenService {

    IssuedRefreshToken issue(UUID userId);

    IssuedRefreshToken rotate(String rawToken);

    void revoke(String rawToken);

    void revokeAll(UUID userId);

    record IssuedRefreshToken(
            UUID userId,
            String token
    ) {
    }
}