package com.example.ledgercore.auth.service;

import com.example.ledgercore.auth.config.RefreshTokenProperties;
import com.example.ledgercore.auth.entity.RefreshToken;
import com.example.ledgercore.auth.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.HexFormat;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private static final int TOKEN_BYTES = 64;

    private final RefreshTokenRepository refreshTokenRepository;
    private final RefreshTokenProperties refreshTokenProperties;
    private final SecureRandom secureRandom;

    @Override
    public IssuedRefreshToken issue(UUID userId) {
        String rawToken = generateToken();

        RefreshToken refreshToken = RefreshToken.builder()
                .userId(userId)
                .tokenHash(hash(rawToken))
                .expiresAt(
                        Instant.now()
                                .plus(refreshTokenProperties.getExpiration())
                )
                .build();

        refreshTokenRepository.save(refreshToken);

        return new IssuedRefreshToken(
                userId,
                rawToken
        );
    }

    @Override
    public IssuedRefreshToken rotate(String rawToken) {
        RefreshToken currentToken = findValidToken(rawToken);

        String newRawToken = generateToken();

        RefreshToken newToken = RefreshToken.builder()
                .userId(currentToken.getUserId())
                .tokenHash(hash(newRawToken))
                .expiresAt(
                        Instant.now()
                                .plus(refreshTokenProperties.getExpiration())
                )
                .build();

        refreshTokenRepository.save(newToken);

        currentToken.revoke();
        currentToken.setReplacedBy(newToken.getId());

        refreshTokenRepository.save(currentToken);

        return new IssuedRefreshToken(
                currentToken.getUserId(),
                newRawToken
        );
    }

    @Override
    public void revoke(String rawToken) {
        RefreshToken refreshToken = findToken(rawToken);

        if (!refreshToken.isRevoked()) {
            refreshToken.revoke();
            refreshTokenRepository.save(refreshToken);
        }
    }

    @Override
    public void revokeAll(UUID userId) {
        refreshTokenRepository
                .findAllByUserIdAndRevokedAtIsNull(userId)
                .forEach(RefreshToken::revoke);
    }

    private RefreshToken findValidToken(String rawToken) {
        RefreshToken refreshToken = findToken(rawToken);

        if (refreshToken.isRevoked()) {
            throw new IllegalArgumentException(
                    "Refresh token has been revoked"
            );
        }

        if (refreshToken.isExpired()) {
            throw new IllegalArgumentException(
                    "Refresh token has expired"
            );
        }

        return refreshToken;
    }

    private RefreshToken findToken(String rawToken) {
        String tokenHash = hash(rawToken);

        return refreshTokenRepository
                .findByTokenHash(tokenHash)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Invalid refresh token"
                        )
                );
    }

    private String generateToken() {
        byte[] bytes = new byte[TOKEN_BYTES];
        secureRandom.nextBytes(bytes);

        return HexFormat.of().formatHex(bytes);
    }

    private String hash(String rawToken) {
        try {
            MessageDigest digest =
                    MessageDigest.getInstance("SHA-256");

            byte[] hash = digest.digest(
                    rawToken.getBytes(StandardCharsets.UTF_8)
            );

            return HexFormat.of().formatHex(hash);

        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(
                    "SHA-256 algorithm is not available",
                    e
            );
        }
    }
}