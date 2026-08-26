package com.example.ledgercore.auth.command.dto;

public record SignUpCommand(
        String displayName,
        String password,
        String email
) {
}