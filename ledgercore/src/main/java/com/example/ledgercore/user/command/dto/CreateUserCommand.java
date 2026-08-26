package com.example.ledgercore.user.command.dto;

public record CreateUserCommand(
        String displayName,
        String email,
        String passwordHash
) {
}