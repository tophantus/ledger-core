package com.example.ledgercore.auth.command.dto;

public record LogoutCommand(
        String refreshToken
) {
}