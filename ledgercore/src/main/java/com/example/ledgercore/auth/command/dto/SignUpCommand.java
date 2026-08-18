package com.example.ledgercore.auth.command.dto;

public record SignUpCommand(
        String username,
        String password,
        String email
) {
}