package com.example.ledgercore.auth.service;

import java.util.UUID;

public interface RefreshTokenService {

    String issue(UUID userId);

    String rotate(String rawToken);

    void revoke(String rawToken);

    void revokeAll(UUID userId);
}