package com.example.ledgercore.auth.service;

import com.example.ledgercore.auth.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class JwtServiceImpl implements JwtService {

    private final JwtProperties jwtProperties;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(
                jwtProperties.getSecret().getBytes()
        );
    }

    @Override
    public String generateAccessToken(
            UUID userId,
            Set<String> roles
    ) {
        Instant now = Instant.now();
        Instant expiration = now.plus(
                jwtProperties.getAccessTokenExpiration()
        );

        return Jwts.builder()
                .subject(userId.toString())
                .claim("roles", roles)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiration))
                .signWith(getSigningKey())
                .compact();
    }

    @Override
    public boolean validateAccessToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public UUID extractUserId(String token) {
        return UUID.fromString(
                parseClaims(token).getSubject()
        );
    }

    @Override
    @SuppressWarnings("unchecked")
    public Set<String> extractRoles(String token) {
        return Set.copyOf(
                parseClaims(token).get("roles", java.util.List.class)
        );
    }

    @Override
    public Instant extractExpiration(String token) {
        return parseClaims(token)
                .getExpiration()
                .toInstant();
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}