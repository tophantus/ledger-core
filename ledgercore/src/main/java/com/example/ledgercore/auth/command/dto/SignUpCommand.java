package com.example.ledgercore.auth.command.dto;

public record SignUpCommand(
        String fullName,
        String password,
        String email
) {
}