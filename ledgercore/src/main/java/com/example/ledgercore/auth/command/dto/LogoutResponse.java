package com.example.ledgercore.auth.command.dto;

public record LogoutResponse(
        boolean loggedOut,
        String message
) {
}