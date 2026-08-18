package com.example.ledgercore.auth.command.dto;

public record TokenResponse(
        String accessToken,
        String refreshToken
) {
}